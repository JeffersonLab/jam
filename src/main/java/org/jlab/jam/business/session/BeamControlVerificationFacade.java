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
import org.jlab.jam.persistence.entity.BeamAuthorization;
import org.jlab.jam.persistence.enumeration.OperationsType;
import org.jlab.jam.persistence.view.BeamExpirationEvent;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.service.UserAuthorizationService;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.persistence.view.User;

/**
 * @author ryans
 */
@Stateless
@DeclareRoles({"jam-admin"})
public class BeamControlVerificationFacade extends AbstractFacade<BeamControlVerification> {

  private static final Logger LOGGER =
      Logger.getLogger(BeamControlVerificationFacade.class.getName());

  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @EJB CreditedControlFacade controlFacade;
  @EJB BeamDestinationFacade destinationFacade;
  @EJB BeamAuthorizationFacade beamAuthorizationFacade;
  @EJB LogbookFacade logbookFacade;
  @EJB EmailFacade emailFacade;
  @EJB FacilityFacade facilityFacade;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public BeamControlVerificationFacade() {
    super(BeamControlVerification.class);
  }

  @PermitAll
  public List<BeamControlVerification> findByBeamDestination(BigInteger beamDestinationId) {
    TypedQuery<BeamControlVerification> q =
        em.createQuery(
            "select a from BeamControlVerification a join fetch a.creditedControl where a.beamDestination.beamDestinationId = :beamDestinationId order by a.creditedControl.weight asc",
            BeamControlVerification.class);

    q.setParameter("beamDestinationId", beamDestinationId);

    return q.getResultList();
  }

  @RolesAllowed("jam-admin")
  public void toggle(BigInteger controlId, BigInteger destinationId) {
    BeamControlVerification verification = find(controlId, destinationId);

    String username = checkAuthenticated();

    if (verification == null) {
      verification = new BeamControlVerification();
      verification.setModifiedBy(username);
      verification.setModifiedDate(new Date());
      CreditedControl control = controlFacade.find(controlId);
      verification.setCreditedControl(control);
      BeamDestination destination = destinationFacade.find(destinationId);
      verification.setBeamDestination(destination);
      verification.setVerificationStatusId(100);
      create(verification);
    } else {
      remove(verification);
    }
  }

  @PermitAll
  public BeamControlVerification find(BigInteger controlId, BigInteger destinationId) {
    TypedQuery<BeamControlVerification> q =
        em.createQuery(
            "select a from BeamControlVerification a where a.creditedControl.creditedControlId = :creditedControlId and a.beamDestination.beamDestinationId = :beamDestinationId",
            BeamControlVerification.class);

    q.setParameter("creditedControlId", controlId);
    q.setParameter("beamDestinationId", destinationId);

    List<BeamControlVerification> verificationList = q.getResultList();

    BeamControlVerification verification = null;

    if (verificationList != null && !verificationList.isEmpty()) {
      verification = verificationList.get(0);
    }

    return verification;
  }

  @PermitAll
  public void edit(
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

    Map<Facility, List<BeamControlVerification>> downgradeMap = new HashMap<>();

    UserAuthorizationService auth = UserAuthorizationService.getInstance();

    for (BigInteger controlVerificationId : controlVerificationIdArray) {
      if (controlVerificationId == null) {
        throw new UserFriendlyException("control verification ID must not be null");
      }

      BeamControlVerification verification = find(controlVerificationId);

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
        Facility facility = verification.getBeamDestination().getFacility();
        List<BeamControlVerification> downgradeList = downgradeMap.get(facility);
        if (downgradeList == null) {
          downgradeList = new ArrayList<>();
          downgradeMap.put(facility, downgradeList);
        }
        downgradeList.add(verification);
      }

      BeamControlVerificationHistory history = new BeamControlVerificationHistory();
      history.setVerificationStatusId(verificationId);
      history.setModifiedBy(username);
      history.setModifiedDate(modifiedDate);
      history.setVerificationDate(verificationDate);
      history.setVerifiedBy(verifiedUsername);
      history.setExpirationDate(expirationDate);
      history.setComments(comments);
      history.setExternalUrl(externalUrl);
      history.setBeamControlVerification(verification);
      em.persist(history);
    }

    if (!downgradeMap.isEmpty()) {
      for (Facility facility : downgradeMap.keySet()) {
        List<BeamControlVerification> downgradeList = downgradeMap.get(facility);
        clearDirectorPermissionForDowngrade(facility, downgradeList);

        emailFacade.sendAsyncBeamVerifierDowngradeEmail(facility, downgradeList);
      }
    }
  }

  @PermitAll
  public List<BeamDestinationAuthorization> checkForAuthorizedButExpired(
      BeamAuthorization mostRecent) {
    TypedQuery<BeamDestinationAuthorization> q =
        em.createQuery(
            "select a from BeamDestinationAuthorization a where a.beamAuthorization.beamAuthorizationId = :authId and a.expirationDate < sysdate and a.beamMode != 'None' and a.destination.active = true order by a.destinationAuthorizationPK.beamDestinationId asc",
            BeamDestinationAuthorization.class);

    BigInteger authId = mostRecent.getBeamAuthorizationId();

    q.setParameter("authId", authId);

    return q.getResultList();
  }

  @PermitAll
  public List<BeamControlVerification> checkForExpired() {
    TypedQuery<BeamControlVerification> q =
        em.createQuery(
            "select a from BeamControlVerification a join fetch a.creditedControl where a.expirationDate < sysdate and a.beamDestination.active = true order by a.creditedControl.weight asc",
            BeamControlVerification.class);

    return q.getResultList();
  }

  @PermitAll
  public List<BeamControlVerification> checkForVerifiedButExpired() {
    TypedQuery<BeamControlVerification> q =
        em.createQuery(
            "select a from BeamControlVerification a join fetch a.creditedControl where a.expirationDate < sysdate and a.beamDestination.active = true and a.verificationStatusId in (1, 50) order by a.creditedControl.weight asc",
            BeamControlVerification.class);

    return q.getResultList();
  }

  @PermitAll
  public void revokeExpiredAuthorizations(
      Facility facility, List<BeamDestinationAuthorization> authorizationList) {
    LOGGER.log(Level.FINEST, "I think I've got something authorization-wise to downgrade");
    this.clearDirectorPermissionByDestinationAuthorization(facility, authorizationList);
  }

  @PermitAll
  public void revokeExpiredVerifications(
      Facility facility, List<BeamControlVerification> expiredList) {

    // Prob should be doing select for update or whatever to ensure concurrent calls to this method
    // don't result in duplicate history records.

    Query q =
        em.createQuery(
            "update BeamControlVerification a set a.verificationStatusId = 100, a.comments = 'Expired', a.verifiedBy = null, a.verificationDate = :vDate, a.modifiedDate = :vDate, a.modifiedBy = '"
                + AUTO_REVOKE_USERNAME
                + "' where a.beamControlVerificationId in :list");

    List<BigInteger> expiredIdList = new ArrayList<>();

    Date modifiedDate = new Date();

    if (expiredList != null) {
      for (BeamControlVerification v : expiredList) {
        expiredIdList.add(v.getBeamControlVerificationId());
      }
    }

    q.setParameter("list", expiredIdList);
    q.setParameter("vDate", modifiedDate);

    q.executeUpdate();

    insertExpiredHistory(expiredList, modifiedDate);

    em.flush();

    clearDirectorPermissionForExpired(facility, expiredList);
  }

  @PermitAll
  public void clearDirectorPermissionForExpired(
      Facility facility, List<BeamControlVerification> verificationList) {
    clearDirectorPermissionByCreditedControl(facility, verificationList, true);
  }

  @PermitAll
  public void clearDirectorPermissionForDowngrade(
      Facility facility, List<BeamControlVerification> verificationList) {
    clearDirectorPermissionByCreditedControl(facility, verificationList, false);
  }

  private void clearDirectorPermissionByCreditedControl(
      Facility facility, List<BeamControlVerification> verificationList, Boolean expiration) {
    String reason = "expiration";

    if (!expiration) {
      reason = "downgrade";
    }

    BeamAuthorization beamAuthorization = beamAuthorizationFacade.findCurrent(facility);

    BeamAuthorization authClone = beamAuthorization.createAdminClone();
    // authClone.setDestinationAuthorizationList(new ArrayList<>());
    List<BeamDestinationAuthorization> newList = new ArrayList<>();

    boolean atLeastOne = false;
    List<String> revokedDestinationList = new ArrayList<>();

    // The destination authorization list will be null if already cleared previously: remember there
    // are two ways in which a clear can happen and they can race to see who clears permissions
    // first:
    // (1) director's expiration vs (2) credited control expiration
    if (beamAuthorization.getDestinationAuthorizationList() != null) {
      for (BeamDestinationAuthorization auth :
          beamAuthorization.getDestinationAuthorizationList()) {
        BeamDestinationAuthorization destClone = auth.createAdminClone(authClone);
        // authClone.getDestinationAuthorizationList().add(destClone);
        newList.add(destClone);

        if ("None".equals(auth.getBeamMode())) {
          continue; // Already None so no need to revoke; move on to next
        }
        BigInteger destinationId = auth.getDestinationAuthorizationPK().getBeamDestinationId();
        for (BeamControlVerification verification : verificationList) {
          if (destinationId.equals(verification.getBeamDestination().getBeamDestinationId())) {
            destClone.setBeamMode("None");
            destClone.setCwLimit(null);
            destClone.setExpirationDate(null);
            destClone.setComments(
                "Permission automatically revoked due to credited control "
                    + verification.getCreditedControl().getName()
                    + " verification "
                    + reason);
            LOGGER.log(Level.FINEST, "Found something to downgrade");
            atLeastOne = true;
            revokedDestinationList.add(destClone.getDestination().getName());
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
      String csv = IOUtil.toCsv(revokedDestinationList.toArray());
      comments = comments + "\nCHANGE: Destination control verification revoked: " + csv;
      authClone.setComments(comments);
      em.persist(authClone);
      for (BeamDestinationAuthorization da : newList) {
        DestinationAuthorizationPK pk = new DestinationAuthorizationPK();
        pk.setBeamDestinationId(da.getDestination().getBeamDestinationId());
        pk.setAuthorizationId(authClone.getBeamAuthorizationId());
        da.setDestinationAuthorizationPK(pk);
        em.persist(da);
      }

      logbookFacade.sendAsyncAuthorizationLogEntry(
          facility, OperationsType.BEAM, authClone.getBeamAuthorizationId());

      // TODO: Send async Downgrade Email notifications?
    }
  }

  private void clearDirectorPermissionByDestinationAuthorization(
      Facility facility, List<BeamDestinationAuthorization> destinationList) {
    BeamAuthorization beamAuthorization = beamAuthorizationFacade.findCurrent(facility);

    BeamAuthorization authClone = beamAuthorization.createAdminClone();
    // authClone.setDestinationAuthorizationList(new ArrayList<>());
    List<BeamDestinationAuthorization> newList = new ArrayList<>();

    boolean atLeastOne = false;
    List<String> revokedDestinationList = new ArrayList<>();

    // The destination authorization list will be null if already cleared previously: remember there
    // are two ways in which a clear can happen and they can race to see who clears permissions
    // first:
    // (1) director's expiration vs (2) credited control expiration
    if (beamAuthorization.getDestinationAuthorizationList() != null) {
      for (BeamDestinationAuthorization auth :
          beamAuthorization.getDestinationAuthorizationList()) {
        BeamDestinationAuthorization destClone = auth.createAdminClone(authClone);
        newList.add(destClone);

        if ("None".equals(auth.getBeamMode())) {
          continue; // Already None so no need to revoke; move on to next
        }
        if (destinationList.contains(auth)) {
          destClone.setBeamMode("None");
          destClone.setCwLimit(null);
          destClone.setExpirationDate(null);
          destClone.setComments(
              "Permission automatically revoked due to director's authorization expiration");
          atLeastOne = true;
          revokedDestinationList.add(destClone.getDestination().getName());
        }
      }
    }

    if (atLeastOne) {
      String comments = authClone.getComments();
      if (comments == null) {
        comments = "";
      }
      String csv = IOUtil.toCsv(revokedDestinationList.toArray());
      comments = comments + "\nCHANGE: Destination authorization revoked: " + csv;
      authClone.setComments(comments);
      em.persist(authClone);
      for (BeamDestinationAuthorization da : newList) {
        DestinationAuthorizationPK pk = new DestinationAuthorizationPK();
        pk.setBeamDestinationId(da.getDestination().getBeamDestinationId());
        pk.setAuthorizationId(authClone.getBeamAuthorizationId());
        da.setDestinationAuthorizationPK(pk);
        em.persist(da);
      }

      logbookFacade.sendAsyncAuthorizationLogEntry(
          facility, OperationsType.BEAM, authClone.getBeamAuthorizationId());
    }
  }

  @PermitAll
  public void insertExpiredHistory(
      List<BeamControlVerification> verificationList, Date modifiedDate) {
    for (BeamControlVerification v : verificationList) {
      BeamControlVerificationHistory history = new BeamControlVerificationHistory();
      history.setModifiedBy(AUTO_REVOKE_USERNAME);
      history.setModifiedDate(modifiedDate);
      history.setVerificationStatusId(100);
      history.setVerificationDate(modifiedDate);
      history.setVerifiedBy(null);
      history.setExpirationDate(v.getExpirationDate());
      history.setComments("Expired");
      history.setBeamControlVerification(v);
      em.persist(history);
    }
  }

  @PermitAll
  public List<BeamControlVerification> checkForUpcomingVerificationExpirations() {
    TypedQuery<BeamControlVerification> q =
        em.createQuery(
            "select a from BeamControlVerification a join fetch a.creditedControl where a.expirationDate >= sysdate and (a.expirationDate - 7) <= sysdate and a.verificationStatusId in (1, 50) and a.beamDestination.active = true order by a.creditedControl.weight asc",
            BeamControlVerification.class);

    return q.getResultList();
  }

  private List<BeamDestinationAuthorization> checkForUpcomingAuthorizationExpirations(
      BeamAuthorization auth) {
    List<BeamDestinationAuthorization> upcomingExpirations = new ArrayList<>();

    Date now = new Date();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, 3);
    Date threeDaysFromNow = cal.getTime();

    if (auth.getDestinationAuthorizationList() != null) {
      for (BeamDestinationAuthorization dest : auth.getDestinationAuthorizationList()) {
        if (!"None".equals(dest.getBeamMode())
            && dest.getExpirationDate().after(now)
            && dest.getExpirationDate().before(threeDaysFromNow)) {
          upcomingExpirations.add(dest);
        }
      }
    }

    return upcomingExpirations;
  }

  @PermitAll
  public BeamExpirationEvent performExpirationCheck(Facility facility, boolean checkForUpcoming) {
    LOGGER.log(Level.FINEST, "Expiration Check: Director's authorizations...");
    BeamAuthorization auth = beamAuthorizationFacade.findCurrent(facility);
    List<BeamDestinationAuthorization> expiredAuthorizationList = null;

    if (auth != null) {
      expiredAuthorizationList = checkForAuthorizedButExpired(auth);
      if (expiredAuthorizationList != null && !expiredAuthorizationList.isEmpty()) {
        LOGGER.log(Level.FINEST, "Expiration Check: Revoking expired authorization");
        revokeExpiredAuthorizations(facility, expiredAuthorizationList);
      }
    }

    LOGGER.log(Level.FINEST, "Expiration Check: Checking for expired verifications...");
    List<BeamControlVerification> expiredVerificationList =
        checkForVerifiedButExpired(); // only items which are "verified" or "provisionally
    // verified", but need to be "not verified" due to expiration
    if (expiredVerificationList != null && !expiredVerificationList.isEmpty()) {
      LOGGER.log(Level.FINEST, "Expiration Check: Revoking expired verifications...");
      revokeExpiredVerifications(facility, expiredVerificationList);
    }

    List<BeamControlVerification> upcomingVerificationExpirationList = null;
    List<BeamDestinationAuthorization> upcomingAuthorizationExpirationList = null;
    if (checkForUpcoming) {
      LOGGER.log(
          Level.FINEST, "Expiration Check: Checking for upcoming verification expirations...");
      upcomingVerificationExpirationList = checkForUpcomingVerificationExpirations();

      LOGGER.log(
          Level.FINEST, "Expiration Check: Checking for upcoming authorization expirations...");
      if (auth != null) {
        upcomingAuthorizationExpirationList = checkForUpcomingAuthorizationExpirations(auth);
      }
    }

    BeamExpirationEvent event = null;

    if (expiredAuthorizationList != null
        || upcomingAuthorizationExpirationList != null
        || expiredVerificationList != null
        || upcomingVerificationExpirationList != null) {
      event =
          new BeamExpirationEvent(
              facility,
              expiredAuthorizationList,
              upcomingAuthorizationExpirationList,
              expiredVerificationList,
              upcomingVerificationExpirationList);
    }

    return event;
  }

  @PermitAll
  public BeamControlVerification findWithCreditedControl(BigInteger controlVerificationId) {
    TypedQuery<BeamControlVerification> q =
        em.createQuery(
            "select a from BeamControlVerification a join fetch a.creditedControl where a.beamControlVerificationId = :id",
            BeamControlVerification.class);

    q.setParameter("id", controlVerificationId);

    List<BeamControlVerification> resultList = q.getResultList();

    BeamControlVerification verification = null;

    if (resultList != null && !resultList.isEmpty()) {
      verification = resultList.get(0);
    }

    return verification;
  }

  @PermitAll
  public void performExpirationCheckAll() {
    List<Facility> facilityList = facilityFacade.findAll();
    for (Facility facility : facilityList) {
      performExpirationCheck(facility, true);
    }
  }
}
