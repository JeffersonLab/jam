package org.jlab.jam.business.session;

import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.jlab.jam.persistence.entity.*;
import org.jlab.jam.persistence.enumeration.OperationsType;
import org.jlab.jam.persistence.view.RFExpirationEvent;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.service.UserAuthorizationService;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.persistence.view.User;

/**
 * @author ryans
 */
@Stateless
@DeclareRoles({"jam-admin"})
public class RFControlVerificationFacade extends AbstractFacade<RFControlVerification> {

  private static final Logger LOGGER =
      Logger.getLogger(RFControlVerificationFacade.class.getName());

  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @EJB CreditedControlFacade controlFacade;
  @EJB RFSegmentFacade segmentFacade;
  @EJB RFAuthorizationFacade rfAuthorizationFacade;
  @EJB LogbookFacade logbookFacade;
  @EJB EmailFacade emailFacade;
  @EJB FacilityFacade facilityFacade;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public RFControlVerificationFacade() {
    super(RFControlVerification.class);
  }

  @PermitAll
  public List<RFControlVerification> findByRFSegment(BigInteger rfSegmentId) {
    TypedQuery<RFControlVerification> q =
        em.createQuery(
            "select a from RFControlVerification a join fetch a.creditedControl where a.rfSegment.rfSegmentId = :rfSegmentId order by a.creditedControl.weight asc",
            RFControlVerification.class);

    q.setParameter("rfSegmentId", rfSegmentId);

    return q.getResultList();
  }

  @RolesAllowed("jam-admin")
  public void toggle(BigInteger controlId, BigInteger segmentId) {
    RFControlVerification verification = find(controlId, segmentId);

    String username = checkAuthenticated();

    if (verification == null) {
      verification = new RFControlVerification();
      verification.setModifiedBy(username);
      verification.setModifiedDate(new Date());
      CreditedControl control = controlFacade.find(controlId);
      verification.setCreditedControl(control);
      RFSegment segment = segmentFacade.find(segmentId);
      verification.setRFSegment(segment);
      verification.setVerificationStatusId(100);
      create(verification);
    } else {
      remove(verification);
    }
  }

  @PermitAll
  public RFControlVerification find(BigInteger controlId, BigInteger segmentId) {
    TypedQuery<RFControlVerification> q =
        em.createQuery(
            "select a from RFControlVerification a where a.creditedControl.creditedControlId = :creditedControlId and a.rfSegment.rfSegmentId = :rfSegmentId",
            RFControlVerification.class);

    q.setParameter("creditedControlId", controlId);
    q.setParameter("rfSegmentId", segmentId);

    List<RFControlVerification> verificationList = q.getResultList();

    RFControlVerification verification = null;

    if (verificationList != null && !verificationList.isEmpty()) {
      verification = verificationList.get(0);
    }

    return verification;
  }

  @PermitAll
  public Map<Facility, List<RFControlVerification>> edit(
      BigInteger[] controlVerificationIdArray,
      Integer verificationId,
      Date verificationDate,
      String verifiedUsername,
      Date expirationDate,
      String comments,
      String externalUrl)
      throws UserFriendlyException {
    String username = checkAuthenticated();

    if (username == null) {
      throw new UserFriendlyException("username must not be empty");
    }

    if (verifiedUsername == null) {
      throw new UserFriendlyException("verified by must not be empty");
    }

    if (verificationId == null) {
      throw new UserFriendlyException("verification status must not be empty");
    }

    if (verificationDate == null) {
      throw new UserFriendlyException("verification date must not be empty");
    }

    if (controlVerificationIdArray == null) {
      throw new UserFriendlyException("control verification ID array must not be empty");
    }

    // Clear Expiration Date if "Not Verified"
    if (expirationDate != null && verificationId == 100) {
      expirationDate = null;
    }

    Date now = new Date();

    if (expirationDate != null && expirationDate.before(now)) {
      throw new UserFriendlyException("expiration date cannot be in the past");
    }

    if (expirationDate == null && verificationId == 1) {
      throw new UserFriendlyException("expiration date required when status is Verified");
    }

    Map<Facility, List<RFControlVerification>> downgradeMap = new HashMap<>();

    UserAuthorizationService auth = UserAuthorizationService.getInstance();

    for (BigInteger controlVerificationId : controlVerificationIdArray) {
      if (controlVerificationId == null) {
        throw new UserFriendlyException("control verification ID must not be null");
      }

      RFControlVerification verification = find(controlVerificationId);

      if (verification == null) {
        throw new UserFriendlyException(
            "control verification with ID " + controlVerificationId + " not found");
      }

      boolean downgrade =
          verification.getVerificationStatusId() != verificationId
              && verification.getVerificationStatusId() < verificationId;

      // If verificationId is changing and it is a downgrade

      Date modifiedDate = new Date();

      String role = verification.getCreditedControl().getVerificationTeam().getDirectoryRoleName();

      List<User> leaders = auth.getUsersInRole(role);

      checkAdminOrGroupLeader(username, leaders);

      verification.setModifiedBy(username);
      verification.setModifiedDate(modifiedDate);
      verification.setVerificationStatusId(verificationId);
      verification.setVerificationDate(verificationDate);
      verification.setVerifiedBy(verifiedUsername);
      verification.setExpirationDate(expirationDate);
      verification.setComments(comments);
      verification.setExternalUrl(externalUrl);

      if (downgrade) {
        Facility facility = verification.getRFSegment().getFacility();
        List<RFControlVerification> downgradeList = downgradeMap.get(facility);
        if (downgradeList == null) {
          downgradeList = new ArrayList<>();
          downgradeMap.put(facility, downgradeList);
        }
        downgradeList.add(verification);
      }

      RFControlVerificationHistory history = new RFControlVerificationHistory();
      history.setVerificationStatusId(verificationId);
      history.setModifiedBy(username);
      history.setModifiedDate(modifiedDate);
      history.setVerificationDate(verificationDate);
      history.setVerifiedBy(verifiedUsername);
      history.setExpirationDate(expirationDate);
      history.setComments(comments);
      history.setExternalUrl(externalUrl);
      history.setRFControlVerification(verification);
      em.persist(history);
    }

    if (!downgradeMap.isEmpty()) {
      for (Facility facility : downgradeMap.keySet()) {
        List<RFControlVerification> downgradeList = downgradeMap.get(facility);
        clearDirectorPermissionForDowngrade(facility, downgradeList);

        emailFacade.sendAsyncRFVerifierDowngradeEmail(facility, downgradeList);
      }
    }

    return downgradeMap;
  }

  @PermitAll
  public List<RFSegmentAuthorization> checkForAuthorizedButExpired(RFAuthorization mostRecent) {
    TypedQuery<RFSegmentAuthorization> q =
        em.createQuery(
            "select a from RFSegmentAuthorization a where a.rfAuthorization.rfAuthorizationId = :authId and a.expirationDate < sysdate and a.highPowerRf = true and a.segment.active = true order by a.segmentAuthorizationPK.rfSegmentId asc",
            RFSegmentAuthorization.class);

    BigInteger authId = mostRecent.getRfAuthorizationId();

    q.setParameter("authId", authId);

    return q.getResultList();
  }

  @PermitAll
  public List<RFControlVerification> checkForExpired() {
    TypedQuery<RFControlVerification> q =
        em.createQuery(
            "select a from RFControlVerification a join fetch a.creditedControl where a.expirationDate < sysdate and a.rfSegment.active = true order by a.creditedControl.weight asc",
            RFControlVerification.class);

    return q.getResultList();
  }

  @PermitAll
  public List<RFControlVerification> checkForVerifiedButExpired(Facility facility) {
    TypedQuery<RFControlVerification> q =
        em.createQuery(
            "select a from RFControlVerification a join fetch a.creditedControl where a.expirationDate < sysdate and a.rfSegment.active = true and a.verificationStatusId in (1, 50) and a.rfSegment.facility = :facility order by a.creditedControl.weight asc",
            RFControlVerification.class);

    q.setParameter("facility", facility);

    return q.getResultList();
  }

  @PermitAll
  public void revokeExpiredVerifications(
      Facility facility, List<RFControlVerification> expiredList) {
    Query q =
        em.createQuery(
            "update RFControlVerification a set a.verificationStatusId = 100, a.comments = 'Expired', a.verifiedBy = null, a.verificationDate = :vDate, a.modifiedDate = :vDate, a.modifiedBy = '"
                + AUTO_REVOKE_USERNAME
                + "' where a.rfControlVerificationId in :list");

    List<BigInteger> expiredIdList = new ArrayList<>();

    Date modifiedDate = new Date();

    if (expiredList != null) {
      for (RFControlVerification v : expiredList) {
        expiredIdList.add(v.getRFControlVerificationId());
      }
    }

    q.setParameter("list", expiredIdList);
    q.setParameter("vDate", modifiedDate);

    q.executeUpdate();

    insertExpiredHistory(expiredList, modifiedDate);

    em.flush();
  }

  @PermitAll
  public void clearDirectorPermissionForExpired(
      Facility facility, List<RFControlVerification> verificationList) {
    clearDirectorPermissionByCreditedControl(facility, verificationList, true);
  }

  @PermitAll
  public void clearDirectorPermissionForDowngrade(
      Facility facility, List<RFControlVerification> verificationList) {
    clearDirectorPermissionByCreditedControl(facility, verificationList, false);
  }

  private void clearDirectorPermissionByCreditedControl(
      Facility facility, List<RFControlVerification> verificationList, Boolean expiration) {
    String reason = "expiration";

    if (!expiration) {
      reason = "downgrade";
    }

    RFAuthorization rfAuthorization = rfAuthorizationFacade.findCurrent(facility);

    if (rfAuthorization == null) {
      LOGGER.log(Level.INFO, "No current RFAuthorization, so nothing to downgrade");
      return;
    }

    RFAuthorization authClone = rfAuthorization.createAdminClone();
    // authClone.setDestinationAuthorizationList(new ArrayList<>());
    List<RFSegmentAuthorization> newList = new ArrayList<>();

    boolean atLeastOne = false;
    List<String> revokedSegmentList = new ArrayList<>();

    // The destination authorization list will be null if already cleared previously: remember there
    // are two ways in which a clear can happen and they can race to see who clears permissions
    // first:
    // (1) director's expiration vs (2) credited control expiration
    if (rfAuthorization.getRFSegmentAuthorizationList() != null) {
      for (RFSegmentAuthorization auth : rfAuthorization.getRFSegmentAuthorizationList()) {
        RFSegmentAuthorization destClone = auth.createAdminClone(authClone);
        // authClone.getDestinationAuthorizationList().add(destClone);
        newList.add(destClone);

        if (!auth.isHighPowerRf()) {
          continue; // Already No High RF Auth so no need to revoke; move on to next
        }
        BigInteger destinationId = auth.getSegmentAuthorizationPK().getRFSegmentId();
        for (RFControlVerification verification : verificationList) {
          if (destinationId.equals(verification.getRFSegment().getRFSegmentId())) {
            destClone.setHighPowerRf(false);
            destClone.setExpirationDate(null);
            destClone.setComments(
                "Permission automatically revoked due to credited control "
                    + verification.getCreditedControl().getName()
                    + " verification "
                    + reason);
            LOGGER.log(Level.FINEST, "Found something to downgrade");
            atLeastOne = true;
            revokedSegmentList.add(destClone.getSegment().getName());
            break; // Found a match so revoke and then break out of loop
          }
        }
      }
    }

    if (atLeastOne) {
      String comments = authClone.getComments();
      if (comments == null) {
        comments = "";
      }
      String csv = IOUtil.toCsv(revokedSegmentList.toArray());
      comments = comments + "\nCHANGE: Segment control verification revoked: " + csv;
      authClone.setComments(comments);
      em.persist(authClone);
      for (RFSegmentAuthorization da : newList) {
        SegmentAuthorizationPK pk = new SegmentAuthorizationPK();
        pk.setRFSegmentId(da.getSegment().getRFSegmentId());
        pk.setRFAuthorizationId(authClone.getRfAuthorizationId());
        da.setSegmentAuthorizationPK(pk);
        em.persist(da);
      }

      logbookFacade.sendAsyncAuthorizationLogEntry(
          facility, OperationsType.RF, authClone.getRfAuthorizationId());
    }
  }

  private void clearDirectorPermissionBySegmentAuthorization(
      Facility facility, List<RFSegmentAuthorization> segmentList) {
    RFAuthorization rfAuthorization = rfAuthorizationFacade.findCurrent(facility);

    RFAuthorization authClone = rfAuthorization.createAdminClone();
    List<RFSegmentAuthorization> newList = new ArrayList<>();

    boolean atLeastOne = false;
    List<String> revokedSegmentList = new ArrayList<>();

    if (rfAuthorization.getRFSegmentAuthorizationList() != null) {
      for (RFSegmentAuthorization auth : rfAuthorization.getRFSegmentAuthorizationList()) {
        RFSegmentAuthorization destClone = auth.createAdminClone(authClone);
        // authClone.getDestinationAuthorizationList().add(destClone);
        newList.add(destClone);

        if (!auth.isHighPowerRf()) {
          continue; // Already No High Power RF auth so no need to revoke; move on to next
        }
        if (segmentList.contains(auth)) {
          destClone.setHighPowerRf(false);
          destClone.setExpirationDate(null);
          destClone.setComments(
              "Permission automatically revoked due to director's authorization expiration");
          LOGGER.log(Level.FINEST, "Found something to downgrade");
          atLeastOne = true;
          revokedSegmentList.add(destClone.getSegment().getName());
        }
      }
    }

    if (atLeastOne) {
      String comments = authClone.getComments();
      if (comments == null) {
        comments = "";
      }
      String csv = IOUtil.toCsv(revokedSegmentList.toArray());
      comments = comments + "\nCHANGE: Segment authorization revoked: " + csv;
      authClone.setComments(comments);
      em.persist(authClone);
      for (RFSegmentAuthorization da : newList) {
        SegmentAuthorizationPK pk = new SegmentAuthorizationPK();
        pk.setRFSegmentId(da.getSegment().getRFSegmentId());
        pk.setRFAuthorizationId(authClone.getRfAuthorizationId());
        da.setSegmentAuthorizationPK(pk);
        em.persist(da);
      }

      logbookFacade.sendAsyncAuthorizationLogEntry(
          facility, OperationsType.RF, authClone.getRfAuthorizationId());
    }
  }

  @PermitAll
  public void insertExpiredHistory(
      List<RFControlVerification> verificationList, Date modifiedDate) {
    for (RFControlVerification v : verificationList) {
      RFControlVerificationHistory history = new RFControlVerificationHistory();
      history.setModifiedBy(AUTO_REVOKE_USERNAME);
      history.setModifiedDate(modifiedDate);
      history.setVerificationStatusId(100);
      history.setVerificationDate(modifiedDate);
      history.setVerifiedBy(null);
      history.setExpirationDate(v.getExpirationDate());
      history.setComments("Expired");
      history.setRFControlVerification(v);
      em.persist(history);
    }
  }

  @PermitAll
  public List<RFControlVerification> checkForUpcomingVerificationExpirations(Facility facility) {
    TypedQuery<RFControlVerification> q =
        em.createQuery(
            "select a from RFControlVerification a join fetch a.creditedControl where a.expirationDate >= sysdate and (a.expirationDate - 7) <= sysdate and a.verificationStatusId in (1, 50) and a.rfSegment.active = true and a.rfSegment.facility = :facility order by a.creditedControl.weight asc",
            RFControlVerification.class);

    q.setParameter("facility", facility);

    return q.getResultList();
  }

  private List<RFSegmentAuthorization> checkForUpcomingAuthorizationExpirations(
      RFAuthorization auth) {
    List<RFSegmentAuthorization> upcomingExpirations = new ArrayList<>();

    Date now = new Date();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, 7);
    Date sevenDaysFromNow = cal.getTime();

    if (auth.getRFSegmentAuthorizationList() != null) {
      for (RFSegmentAuthorization dest : auth.getRFSegmentAuthorizationList()) {
        if (dest.isHighPowerRf()
            && dest.getExpirationDate().after(now)
            && dest.getExpirationDate().before(sevenDaysFromNow)) {
          upcomingExpirations.add(dest);
        }
      }
    }

    return upcomingExpirations;
  }

  @PermitAll
  public RFExpirationEvent performExpirationCheck(Facility facility, boolean checkForUpcoming) {
    LOGGER.log(Level.FINEST, "Expiration Check: Director's authorizations...");
    RFAuthorization auth = rfAuthorizationFacade.findCurrent(facility);
    List<RFSegmentAuthorization> expiredAuthorizationList = null;

    if (auth != null) {
      expiredAuthorizationList = checkForAuthorizedButExpired(auth);
      if (expiredAuthorizationList != null && !expiredAuthorizationList.isEmpty()) {
        LOGGER.log(Level.FINEST, "Expiration Check: Revoking expired authorization");

        clearDirectorPermissionBySegmentAuthorization(facility, expiredAuthorizationList);
      }
    }

    LOGGER.log(Level.FINEST, "Expiration Check: Checking for expired verifications...");
    List<RFControlVerification> expiredVerificationList =
        checkForVerifiedButExpired(facility); // only items which are "verified" or "provisionally
    // verified", but need to be "not verified" due to expiration
    if (expiredVerificationList != null && !expiredVerificationList.isEmpty()) {
      LOGGER.log(Level.FINEST, "Expiration Check: Revoking expired verifications...");
      revokeExpiredVerifications(facility, expiredVerificationList);

      clearDirectorPermissionForExpired(facility, expiredVerificationList);
    }

    List<RFControlVerification> upcomingVerificationExpirationList = null;
    List<RFSegmentAuthorization> upcomingAuthorizationExpirationList = null;
    if (checkForUpcoming) {
      LOGGER.log(
          Level.FINEST, "Expiration Check: Checking for upcoming verification expirations...");
      upcomingVerificationExpirationList = checkForUpcomingVerificationExpirations(facility);

      LOGGER.log(
          Level.FINEST, "Expiration Check: Checking for upcoming authorization expirations...");
      if (auth != null) {
        upcomingAuthorizationExpirationList = checkForUpcomingAuthorizationExpirations(auth);
      }
    }

    RFExpirationEvent event = null;

    if (expiredAuthorizationList != null
        || upcomingAuthorizationExpirationList != null
        || expiredVerificationList != null
        || upcomingVerificationExpirationList != null) {
      event =
          new RFExpirationEvent(
              facility,
              expiredAuthorizationList,
              upcomingAuthorizationExpirationList,
              expiredVerificationList,
              upcomingVerificationExpirationList);
    }

    return event;
  }

  @PermitAll
  public RFControlVerification findWithCreditedControl(BigInteger controlVerificationId) {
    TypedQuery<RFControlVerification> q =
        em.createQuery(
            "select a from RFControlVerification a join fetch a.creditedControl where a.rfControlVerificationId = :id",
            RFControlVerification.class);

    q.setParameter("id", controlVerificationId);

    List<RFControlVerification> resultList = q.getResultList();

    RFControlVerification verification = null;

    if (resultList != null && !resultList.isEmpty()) {
      verification = resultList.get(0);
    }

    return verification;
  }

  @PermitAll
  public Map<Facility, RFExpirationEvent> performExpirationCheckAll() {
    List<Facility> facilityList = facilityFacade.findAll();
    Map<Facility, RFExpirationEvent> eventMap = new HashMap<>();
    for (Facility facility : facilityList) {
      RFExpirationEvent event = performExpirationCheck(facility, true);
      eventMap.put(facility, event);
    }

    return eventMap;
  }
}
