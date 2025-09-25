package org.jlab.jam.business.session;

import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.jam.persistence.entity.*;
import org.jlab.jam.persistence.entity.BeamAuthorization;
import org.jlab.jam.persistence.view.BeamExpirationEvent;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.service.UserAuthorizationService;
import org.jlab.smoothness.persistence.view.User;

/**
 * @author ryans
 */
@Stateless
@DeclareRoles({"jam-admin"})
public class BeamControlVerificationFacade extends AbstractFacade<BeamControlVerification> {

  private static final Logger LOGGER =
      Logger.getLogger(BeamControlVerificationFacade.class.getName());

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @EJB CreditedControlFacade controlFacade;
  @EJB BeamDestinationFacade destinationFacade;
  @EJB BeamAuthorizationFacade beamAuthorizationFacade;
  @EJB LogbookFacade logbookFacade;
  @EJB NotificationManager notificationManager;

  private final ReducedBeamAuthorizationBuilder reducedAuthBuilder =
      new ReducedBeamAuthorizationBuilder();

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
  public List<BeamControlVerification> checkForVerifiedButExpired(Facility facility) {
    TypedQuery<BeamControlVerification> q =
        em.createQuery(
            "select a from BeamControlVerification a join fetch a.creditedControl where a.expirationDate < sysdate and a.beamDestination.active = true and a.verificationStatusId in (1, 50) and a.beamDestination.facility = :facility order by a.creditedControl.weight asc",
            BeamControlVerification.class);

    q.setParameter("facility", facility);

    return q.getResultList();
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
  }

  @PermitAll
  public void clearDirectorPermissionForDowngrade(
      Facility facility, List<BeamControlVerification> verificationList) {

    BeamAuthorization beamAuthorization = beamAuthorizationFacade.findCurrent(facility);

    if (beamAuthorization == null) {
      LOGGER.log(Level.INFO, "No current BeamAuthorization, so nothing to downgrade");
      return;
    }

    BeamAuthorization authReduction =
        reducedAuthBuilder.build(beamAuthorization, facility, null, verificationList, false);

    if (authReduction != null) {
      saveReducedAuth(authReduction);

      notificationManager.asyncNotifyBeamVerificationDowngrade(
          facility, verificationList, authReduction);
    }
  }

  public void saveReducedAuth(BeamAuthorization auth) {

    // Stash list in temporary variable as auth must be persisted BEFORE list is set
    List<BeamDestinationAuthorization> operationsList = auth.getDestinationAuthorizationList();

    auth.setDestinationAuthorizationList(null);

    em.persist(auth);

    if (operationsList != null) {
      for (BeamDestinationAuthorization operationAuth : operationsList) {
        DestinationAuthorizationPK pk = new DestinationAuthorizationPK();
        pk.setBeamDestinationId(operationAuth.getDestination().getBeamDestinationId());
        operationAuth.setDestinationAuthorizationPK(pk);
        pk.setAuthorizationId(auth.getBeamAuthorizationId());
        em.persist(operationAuth);
      }
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
  public List<BeamControlVerification> checkForUpcomingVerificationExpirations(
      Facility facility, boolean boundary) {
    String dateRangeConstraint =
        "(sysdate) <= a.expirationDate and (sysdate + 7) > a.expirationDate";

    if (boundary) {
      dateRangeConstraint =
          "(sysdate + 6) <= a.expirationDate and (sysdate + 7) > a.expirationDate";
    }

    TypedQuery<BeamControlVerification> q =
        em.createQuery(
            "select a from BeamControlVerification a join fetch a.creditedControl where "
                + dateRangeConstraint
                + " and a.verificationStatusId in (1, 50) and a.beamDestination.active = true and a.beamDestination.facility = :facility order by a.creditedControl.weight asc",
            BeamControlVerification.class);

    q.setParameter("facility", facility);

    // sysdate >= (a.expirationDate - 7) and sysdate < (a.expirationDate - 6)
    // (sysdate + 6) <= a.expirationDate and (sysdate + 7) > a.expirationDate

    List<BeamControlVerification> list = q.getResultList();

    /*for (BeamControlVerification verification : list) {
      System.err.println(
          "Found upcoming Beam operations verification expiration: "
              + verification.getBeamDestination().getName());
    }*/

    return list;

    // return q.getResultList();
  }

  @PermitAll
  public List<BeamDestinationAuthorization> checkForUpcomingAuthorizationExpirations(
      BeamAuthorization auth, boolean boundary) {
    List<BeamDestinationAuthorization> upcomingExpirations = new ArrayList<>();

    Date now = new Date();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, 7);
    Date sevenDaysFromNow = cal.getTime();
    cal.add(Calendar.DATE, -1);
    Date sixDaysFromNow = cal.getTime();

    if (auth.getDestinationAuthorizationList() != null) {
      for (BeamDestinationAuthorization dest : auth.getDestinationAuthorizationList()) {
        if (!"None".equals(dest.getBeamMode()) && dest.getExpirationDate() != null) {
          boolean include = false;
          if (boundary) {
            if ((sixDaysFromNow.before(dest.getExpirationDate())
                    || sixDaysFromNow.equals(dest.getExpirationDate()))
                && sevenDaysFromNow.after(dest.getExpirationDate())) {
              include = true;
            }
          } else {
            if (now.before(dest.getExpirationDate())
                || now.equals(dest.getExpirationDate())
                    && sevenDaysFromNow.after(dest.getExpirationDate())) {
              include = true;
            }
          }

          if (include) {
            upcomingExpirations.add(dest);

            /*System.err.println(
            "Found upcoming Beam operations authorization expiration: "
                + dest.getDestination().getName());*/
          }
        }
      }
    }

    return upcomingExpirations;
  }

  @PermitAll
  public BeamExpirationEvent performExpirationCheck(Facility facility) {
    BeamAuthorization auth = beamAuthorizationFacade.findCurrent(facility);
    List<BeamDestinationAuthorization> expiredAuthorizationList = null;

    List<BeamControlVerification> expiredVerificationList = checkForVerifiedButExpired(facility);
    if (expiredVerificationList != null && !expiredVerificationList.isEmpty()) {
      revokeExpiredVerifications(facility, expiredVerificationList);
    }

    BeamAuthorization authReduction = null;
    if (auth != null) {
      expiredAuthorizationList = checkForAuthorizedButExpired(auth);

      authReduction =
          reducedAuthBuilder.build(
              auth, facility, expiredAuthorizationList, expiredVerificationList, true);

      if (authReduction != null) {
        saveReducedAuth(authReduction);
      }
    }

    BeamExpirationEvent event = null;

    if (authReduction != null) {
      event =
          new BeamExpirationEvent(
              authReduction, facility, expiredAuthorizationList, expiredVerificationList);
    } else {
      event = new BeamExpirationEvent(facility);
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
}
