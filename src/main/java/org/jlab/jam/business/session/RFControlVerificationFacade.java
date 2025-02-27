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
  @EJB NotificationManager notificationManager;
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
  public void clearDirectorPermissionForDowngrade(
      Facility facility, List<RFControlVerification> verificationList) {

    RFAuthorization rfAuthorization = rfAuthorizationFacade.findCurrent(facility);

    if (rfAuthorization == null) {
      LOGGER.log(Level.INFO, "No current RFAuthorization, so nothing to downgrade");
      return;
    }

    RFAuthorization authReduction = createClone(rfAuthorization);

    boolean updated =
        populateReducedPermissionsDueToVerification(
            authReduction, facility, verificationList, false);

    if (updated) {
      saveClone(authReduction);

      notificationManager.asyncNotifyRFVerificationDowngrade(
          facility, verificationList, authReduction);
    }
  }

  private boolean populateReducedPermissionsDueToVerification(
      RFAuthorization clone,
      Facility facility,
      List<RFControlVerification> verificationList,
      Boolean expiration) {
    String reason = "expiration";

    if (!expiration) {
      reason = "downgrade";
    }

    boolean atLeastOne = false;
    List<String> revokedSegmentList = new ArrayList<>();

    if (clone.getRFSegmentAuthorizationList() != null) {
      for (RFSegmentAuthorization operationAuth : clone.getRFSegmentAuthorizationList()) {

        if (!operationAuth.isHighPowerRf()) {
          continue; // Already No High RF Auth so no need to revoke; move on to next
        }
        BigInteger destinationId = operationAuth.getSegmentAuthorizationPK().getRFSegmentId();
        for (RFControlVerification verification : verificationList) {
          if (destinationId.equals(verification.getRFSegment().getRFSegmentId())) {
            operationAuth.setHighPowerRf(false);
            operationAuth.setExpirationDate(null);
            operationAuth.setComments(
                "Permission automatically revoked due to credited control "
                    + verification.getCreditedControl().getName()
                    + " verification "
                    + reason);
            LOGGER.log(Level.FINEST, "Found something to downgrade");
            atLeastOne = true;
            revokedSegmentList.add(operationAuth.getSegment().getName());
            break; // Found a match so revoke and then break out of loop
          }
        }
      }
    }

    if (atLeastOne) {
      String comments = clone.getComments();
      if (comments == null) {
        comments = "";
      }
      String csv = IOUtil.toCsv(revokedSegmentList.toArray());
      comments = comments + "\nCHANGE: Segment control verification revoked: " + csv;
      clone.setComments(comments);
    }

    return atLeastOne;
  }

  private RFAuthorization createClone(RFAuthorization auth) {
    RFAuthorization authClone = auth.createAdminClone();
    List<RFSegmentAuthorization> newList = new ArrayList<>();

    if (auth.getRFSegmentAuthorizationList() != null) {
      for (RFSegmentAuthorization operationAuth : auth.getRFSegmentAuthorizationList()) {
        RFSegmentAuthorization operationClone = operationAuth.createAdminClone(authClone);
        newList.add(operationClone);
      }
    }

    authClone.setRFSegmentAuthorizationList(newList);

    return authClone;
  }

  private void saveClone(RFAuthorization auth) {
    em.persist(auth);
    if (auth.getRFSegmentAuthorizationList() != null) {
      for (RFSegmentAuthorization operationAuth : auth.getRFSegmentAuthorizationList()) {
        SegmentAuthorizationPK pk = new SegmentAuthorizationPK();
        pk.setRFSegmentId(operationAuth.getSegment().getRFSegmentId());
        pk.setRFAuthorizationId(auth.getRfAuthorizationId());
        operationAuth.setSegmentAuthorizationPK(pk);
        em.persist(operationAuth);
      }
    }
  }

  private boolean populateReducedPermissionDueToAuthorizationExpiration(
      RFAuthorization clone, Facility facility, List<RFSegmentAuthorization> segmentList) {

    boolean atLeastOne = false;
    List<String> revokedSegmentList = new ArrayList<>();

    if (clone.getRFSegmentAuthorizationList() != null) {
      for (RFSegmentAuthorization operationAuth : clone.getRFSegmentAuthorizationList()) {

        if (!operationAuth.isHighPowerRf()) {
          continue; // Already No High Power RF auth so no need to revoke; move on to next
        }
        if (segmentList.contains(operationAuth)) {
          operationAuth.setHighPowerRf(false);
          operationAuth.setExpirationDate(null);
          operationAuth.setComments(
              "Permission automatically revoked due to director's authorization expiration");
          LOGGER.log(Level.FINEST, "Found something to downgrade");
          atLeastOne = true;
          revokedSegmentList.add(operationAuth.getSegment().getName());
        }
      }
    }

    if (atLeastOne) {
      String comments = clone.getComments();
      if (comments == null) {
        comments = "";
      }
      String csv = IOUtil.toCsv(revokedSegmentList.toArray());
      comments = comments + "\nCHANGE: Segment authorization revoked: " + csv;
      clone.setComments(comments);
    }

    return atLeastOne;
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
    RFAuthorization auth = rfAuthorizationFacade.findCurrent(facility);
    List<RFSegmentAuthorization> expiredAuthorizationList = null;

    List<RFControlVerification> expiredVerificationList = checkForVerifiedButExpired(facility);
    if (expiredVerificationList != null && !expiredVerificationList.isEmpty()) {
      revokeExpiredVerifications(facility, expiredVerificationList);
    }

    RFAuthorization authReduction = null;
    if (auth != null) {
      authReduction = createClone(auth);
      boolean authorizerReduction = false;
      boolean verifierReduction = false;

      expiredAuthorizationList = checkForAuthorizedButExpired(auth);
      if (expiredAuthorizationList != null && !expiredAuthorizationList.isEmpty()) {

        authorizerReduction =
            populateReducedPermissionDueToAuthorizationExpiration(
                authReduction, facility, expiredAuthorizationList);
      }

      if (expiredVerificationList != null && !expiredVerificationList.isEmpty()) {
        verifierReduction =
            populateReducedPermissionsDueToVerification(
                authReduction, facility, expiredVerificationList, true);
      }

      if (authorizerReduction || verifierReduction) {
        saveClone(authReduction);
      } else {
        authReduction = null;
      }
    }

    List<RFControlVerification> upcomingVerificationExpirationList = null;
    List<RFSegmentAuthorization> upcomingAuthorizationExpirationList = null;
    if (checkForUpcoming) {
      upcomingVerificationExpirationList = checkForUpcomingVerificationExpirations(facility);
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
              authReduction,
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
}
