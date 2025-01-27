package org.jlab.jam.business.session;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.jlab.jam.persistence.entity.*;
import org.jlab.jam.persistence.entity.BeamAuthorization;
import org.jlab.jlog.Body;
import org.jlab.jlog.Library;
import org.jlab.jlog.LogEntry;
import org.jlab.jlog.LogEntryAdminExtension;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.service.EmailService;
import org.jlab.smoothness.business.service.UserAuthorizationService;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.view.User;
import org.jlab.smoothness.presentation.util.Functions;

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
  @EJB BeamDestinationFacade beamDestinationFacade;

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
  public List<BeamControlVerification> edit(
      BigInteger[] controlVerificationIdArray,
      Integer verificationId,
      Date verificationDate,
      String verifiedUsername,
      Date expirationDate,
      String comments)
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

    Date now = new Date();

    if (expirationDate != null && expirationDate.before(now)) {
      throw new UserFriendlyException("expiration date cannot be in the past");
    }

    List<BeamControlVerification> downgradeList = new ArrayList<>();

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

      String role = verification.getCreditedControl().getGroup().getLeaderRoleName();

      List<User> leaders = auth.getUsersInRole(role);

      checkAdminOrGroupLeader(username, leaders);

      verification.setModifiedBy(username);
      verification.setModifiedDate(modifiedDate);
      verification.setVerificationStatusId(verificationId);
      verification.setVerificationDate(verificationDate);
      verification.setVerifiedBy(verifiedUsername);
      verification.setExpirationDate(expirationDate);
      verification.setComments(comments);

      if (downgrade) {
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
      history.setBeamControlVerification(verification);
      em.persist(history);
    }

    if (!downgradeList.isEmpty()) {
      clearDirectorPermissionForDowngrade(downgradeList);
    }

    return downgradeList;
  }

  @PermitAll
  public String getExpiredMessageBody(
      String proxyServer,
      List<BeamDestinationAuthorization> expiredAuthorizationList,
      List<BeamControlVerification> expiredVerificationList,
      List<BeamDestinationAuthorization> upcomingAuthorizationExpirationList,
      List<BeamControlVerification> upcomingVerificationExpirationList) {
    StringBuilder builder = new StringBuilder();

    SimpleDateFormat formatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    if (expiredAuthorizationList != null && !expiredAuthorizationList.isEmpty()) {
      builder.append("<h1>--- Expired Director's Authorizations ---</h1>\n");
      for (BeamDestinationAuthorization authorization : expiredAuthorizationList) {
        builder.append("</div>\n<div><b>Beam Destination:</b> ");
        builder.append(authorization.getDestination().getName());
        builder.append("</div>\n<div><b>Expired On:</b> ");
        builder.append(formatter.format(authorization.getExpirationDate()));
        builder.append("</div>\n<div><b>Comments:</b> ");
        builder.append(
            IOUtil.escapeXml(
                authorization.getComments() == null ? "" : authorization.getComments()));
        builder.append("<br/><br/>\n");
      }
    }

    if (expiredVerificationList != null && !expiredVerificationList.isEmpty()) {
      builder.append("<h1>--- Expired Credited Control Verifications ---</h1>\n");

      for (BeamControlVerification v : expiredVerificationList) {

        builder.append("<div><b>Credited Control:</b> ");
        builder.append(v.getCreditedControl().getName());
        builder.append("</div>\n<div><b>Beam Destination:</b> ");
        builder.append(v.getBeamDestination().getName());
        builder.append("</div>\n<div><b>Verified On:</b> ");
        builder.append(formatter.format(v.getVerificationDate()));
        builder.append("</div>\n<div><b>Verified By:</b> ");
        builder.append(Functions.formatUsername(v.getVerifiedBy()));
        builder.append("</div>\n<div><b>Expired On:</b> ");
        builder.append(formatter.format(v.getExpirationDate()));
        builder.append("</div>\n<div><b>Comments:</b> ");
        builder.append(IOUtil.escapeXml(v.getComments() == null ? "" : v.getComments()));
        builder.append("<br/><br/>\n");
      }

      builder.append("<br/><br/>\n");
    }

    if (upcomingAuthorizationExpirationList != null
        && !upcomingAuthorizationExpirationList.isEmpty()) {
      builder.append("<h1>--- Director's Authorizations Expiring Soon ---</h1>\n");

      for (BeamDestinationAuthorization authorization : upcomingAuthorizationExpirationList) {

        builder.append("<div><b>Beam Destination:</b> ");
        builder.append(authorization.getDestination().getName());
        builder.append("</div>\n<div><b>Expires On:</b> ");
        builder.append(formatter.format(authorization.getExpirationDate()));
        builder.append("</div>\n<div><b>Comments:</b> ");
        builder.append(
            IOUtil.escapeXml(
                authorization.getComments() == null ? "" : authorization.getComments()));
        builder.append("<br/><br/>\n");
      }

      builder.append("<br/><br/>\n");
    }

    if (upcomingVerificationExpirationList != null
        && !upcomingVerificationExpirationList.isEmpty()) {
      builder.append("<h1>--- Credited Control Verifications Expiring Soon ---</h1>\n");

      for (BeamControlVerification v : upcomingVerificationExpirationList) {

        builder.append("<div><b>Credited Control:</b> ");
        builder.append(v.getCreditedControl().getName());
        builder.append("</div>\n<div><b>Beam Destination:</b> ");
        builder.append(v.getBeamDestination().getName());
        builder.append("</div>\n<div><b>Verified On:</b> ");
        builder.append(formatter.format(v.getVerificationDate()));
        builder.append("</div>\n<div><b>Verified By:</b> ");
        builder.append(Functions.formatUsername(v.getVerifiedBy()));
        builder.append("</div>\n<div><b>Expiring On:</b> ");
        builder.append(formatter.format(v.getExpirationDate()));
        builder.append("</div>\n<div><b>Comments:</b> ");
        builder.append(IOUtil.escapeXml(v.getComments() == null ? "" : v.getComments()));
        builder.append("<br/><br/>\n");
      }
    }

    builder.append("<br/><br/>\n");
    builder
        .append("</div><div>\n\n<b>See:</b> <a href=\"")
        .append(proxyServer)
        .append("/jam/\">JLab Authorization Manager</a></div>\n");

    return builder.toString();
  }

  @PermitAll
  public String getVerificationDowngradedMessageBody(
      String proxyServer, List<BeamControlVerification> downgradeList) {
    StringBuilder builder = new StringBuilder();

    SimpleDateFormat formatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    BeamControlVerification verification = downgradeList.get(0);

    builder.append("<div><b>Credited Control:</b> ");
    builder.append(verification.getCreditedControl().getName());
    builder.append("</div>\n<div><b>Beam Destinations:</b> ");
    for (BeamControlVerification v : downgradeList) {
      builder.append("<div>");
      builder.append(v.getBeamDestination().getName());
      builder.append("</div>");
    }
    builder.append("</div>\n<div><b>Modified On:</b> ");
    builder.append(formatter.format(verification.getVerificationDate()));
    builder.append("</div>\n<div><b>Modified By:</b> ");
    builder.append(Functions.formatUsername(verification.getVerifiedBy()));
    builder.append("</div>\n<div><b>Verification:</b> ");
    builder.append(
        verification.getVerificationStatusId() == 1
            ? "Verified"
            : (verification.getVerificationStatusId() == 50
                ? "Provisionally Verified"
                : "Not Verified"));
    builder.append("</div>\n<div><b>Comments:</b> ");
    builder.append(IOUtil.escapeXml(verification.getComments()));
    builder
        .append("</div><div>\n\n<b>See:</b> <a href=\"")
        .append(proxyServer)
        .append("/jam/\">JLab Authorization Manager</a></div>\n");

    return builder.toString();
  }

  @PermitAll
  public long sendVerificationDowngradedELog(String body, String logbookServer)
      throws UserFriendlyException {
    String username = checkAuthenticated();

    String subject = System.getenv("BAM_DOWNGRADED_SUBJECT");

    String logbooks = System.getenv("BAM_BOOKS_CSV");

    if (logbooks == null || logbooks.isEmpty()) {
      logbooks = "TLOG";
      LOGGER.log(
          Level.WARNING, "Environment variable 'BAM_BOOKS_CSV' not found, using default TLOG");
    }

    Properties config = Library.getConfiguration();

    config.setProperty("SUBMIT_URL", logbookServer + "/incoming");
    config.setProperty("FETCH_URL", logbookServer + "/entry");

    LogEntry entry = new LogEntry(subject, logbooks);

    entry.setBody(body, Body.ContentType.HTML);
    entry.setTags("Readme");

    LogEntryAdminExtension extension = new LogEntryAdminExtension(entry);
    extension.setAuthor(username);

    long logId;

    try {
      logId = entry.submitNow();
    } catch (Exception e) {
      throw new UserFriendlyException("Unable to send elog", e);
    }

    return logId;
  }

  @PermitAll
  public void sendVerificationDowngradedEmail(String body) throws UserFriendlyException {
    String toCsv = System.getenv("BAM_DOWNGRADED_EMAIL_CSV");

    String subject = System.getenv("BAM_DOWNGRADED_SUBJECT");

    EmailService emailService = new EmailService();

    String sender = System.getenv("BAM_EMAIL_SENDER");

    emailService.sendEmail(sender, sender, toCsv, null, subject, body, true);
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
  public void revokeExpiredAuthorizations(List<BeamDestinationAuthorization> authorizationList) {
    LOGGER.log(Level.FINEST, "I think I've got something authorization-wise to downgrade");
    this.clearDirectorPermissionByDestinationAuthorization(authorizationList);
  }

  @PermitAll
  public void revokeExpiredVerifications(List<BeamControlVerification> expiredList) {
    Query q =
        em.createQuery(
            "update BeamControlVerification a set a.verificationId = 100, a.comments = 'Expired', a.verifiedBy = null, a.verificationDate = :vDate, a.modifiedDate = :vDate, a.modifiedBy = 'authadm' where a.controlVerificationId in :list");

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

    clearDirectorPermissionForExpired(expiredList);
  }

  @PermitAll
  public void clearDirectorPermissionForExpired(List<BeamControlVerification> verificationList) {
    clearDirectorPermissionByCreditedControl(verificationList, true);
  }

  @PermitAll
  public void clearDirectorPermissionForDowngrade(List<BeamControlVerification> verificationList) {
    clearDirectorPermissionByCreditedControl(verificationList, false);
  }

  private void clearDirectorPermissionByCreditedControl(
      List<BeamControlVerification> verificationList, Boolean expiration) {
    String reason = "expiration";

    if (!expiration) {
      reason = "downgrade";
    }

    BeamAuthorization beamAuthorization = beamAuthorizationFacade.findCurrent();

    BeamAuthorization authClone = beamAuthorization.createAdminClone();
    // authClone.setDestinationAuthorizationList(new ArrayList<>());
    List<BeamDestinationAuthorization> newList = new ArrayList<>();

    boolean atLeastOne = false;

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
            destClone.setComments(
                "Permission automatically revoked due to group credited control verification "
                    + reason);
            LOGGER.log(Level.FINEST, "Found something to downgrade");
            atLeastOne = true;
            break; // Found a match so revoke and then break out of loop
          }
        }
      }
    }

    if (atLeastOne) {
      em.persist(authClone);
      for (BeamDestinationAuthorization da : newList) {
        DestinationAuthorizationPK pk = new DestinationAuthorizationPK();
        pk.setBeamDestinationId(da.getDestination().getBeamDestinationId());
        pk.setAuthorizationId(authClone.getBeamAuthorizationId());
        da.setDestinationAuthorizationPK(pk);
        em.persist(da);
      }
    }
  }

  private void clearDirectorPermissionByDestinationAuthorization(
      List<BeamDestinationAuthorization> destinationList) {
    BeamAuthorization beamAuthorization = beamAuthorizationFacade.findCurrent();

    BeamAuthorization authClone = beamAuthorization.createAdminClone();
    // authClone.setDestinationAuthorizationList(new ArrayList<>());
    List<BeamDestinationAuthorization> newList = new ArrayList<>();

    boolean atLeastOne = false;

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
        if (destinationList.contains(auth)) {
          destClone.setBeamMode("None");
          destClone.setCwLimit(null);
          destClone.setComments(
              "Permission automatically revoked due to director's authorization expiration");
          LOGGER.log(Level.FINEST, "Found something to downgrade");
          atLeastOne = true;
        }
      }
    }

    if (atLeastOne) {
      em.persist(authClone);
      for (BeamDestinationAuthorization da : newList) {
        DestinationAuthorizationPK pk = new DestinationAuthorizationPK();
        pk.setBeamDestinationId(da.getDestination().getBeamDestinationId());
        pk.setAuthorizationId(authClone.getBeamAuthorizationId());
        da.setDestinationAuthorizationPK(pk);
        em.persist(da);
      }
    }
  }

  @PermitAll
  public void insertExpiredHistory(
      List<BeamControlVerification> verificationList, Date modifiedDate) {
    for (BeamControlVerification v : verificationList) {
      BeamControlVerificationHistory history = new BeamControlVerificationHistory();
      history.setModifiedBy("jam-admin");
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
  public void notifyAdmins(
      List<BeamDestinationAuthorization> expiredAuthorizationList,
      List<BeamControlVerification> expiredVerificationList,
      List<BeamDestinationAuthorization> upcomingAuthorizationExpirationList,
      List<BeamControlVerification> upcomingVerificationExpirationList,
      String proxyServer)
      throws MessagingException, UserFriendlyException {
    String toCsv = System.getenv("BAM_UPCOMING_EXPIRATION_EMAIL_CSV");

    String subject = System.getenv("BAM_UPCOMING_EXPIRATION_SUBJECT");

    String body =
        getExpiredMessageBody(
            proxyServer,
            expiredAuthorizationList,
            expiredVerificationList,
            upcomingAuthorizationExpirationList,
            upcomingVerificationExpirationList);

    EmailService emailService = new EmailService();

    String sender = System.getenv("BAM_EMAIL_SENDER");

    emailService.sendEmail(sender, sender, toCsv, null, subject, body, true);
  }

  @PermitAll
  public void notifyOps(
      List<BeamDestinationAuthorization> expiredAuthorizationList,
      List<BeamControlVerification> expiredVerificationList,
      String proxyServer)
      throws MessagingException, UserFriendlyException {
    String toCsv = System.getenv("BAM_EXPIRED_EMAIL_CSV");

    String subject = System.getenv("BAM_EXPIRED_SUBJECT");

    String body =
        getExpiredMessageBody(
            proxyServer, expiredAuthorizationList, expiredVerificationList, null, null);

    EmailService emailService = new EmailService();

    String sender = System.getenv("BAM_EMAIL_SENDER");

    emailService.sendEmail(sender, sender, toCsv, null, subject, body, true);
    LOGGER.log(Level.FINEST, "notifyOps, toCsv: {0], body: {1}", new Object[] {toCsv, body});
  }

  @PermitAll
  public void notifyGroups(
      List<BeamControlVerification> expiredList,
      List<BeamControlVerification> upcomingExpirationsList,
      String proxyServer)
      throws MessagingException, UserFriendlyException {
    Map<Workgroup, List<BeamControlVerification>> expiredGroupMap = new HashMap<>();
    Map<Workgroup, List<BeamControlVerification>> upcomingExpirationGroupMap = new HashMap<>();

    String subject = System.getenv("BAM_UPCOMING_EXPIRATION_SUBJECT");

    LOGGER.log(Level.FINEST, "Expirations:");
    if (expiredList != null) {
      for (BeamControlVerification c : expiredList) {
        LOGGER.log(Level.FINEST, c.toString());
        Workgroup workgroup = c.getCreditedControl().getGroup();
        List<BeamControlVerification> groupList = expiredGroupMap.get(workgroup);
        if (groupList == null) {
          groupList = new ArrayList<>();
          expiredGroupMap.put(workgroup, groupList);
        }
        groupList.add(c);
      }
    } else {
      LOGGER.log(Level.FINEST, "No expirations");
    }

    LOGGER.log(Level.FINEST, "Upcoming Expirations:");
    if (upcomingExpirationsList != null) {
      for (BeamControlVerification c : upcomingExpirationsList) {
        LOGGER.log(Level.FINEST, c.toString());
        Workgroup workgroup = c.getCreditedControl().getGroup();
        List<BeamControlVerification> groupList = upcomingExpirationGroupMap.get(workgroup);
        if (groupList == null) {
          groupList = new ArrayList<>();
          upcomingExpirationGroupMap.put(workgroup, groupList);
        }
        groupList.add(c);
      }
    } else {
      LOGGER.log(Level.FINEST, "No upcoming expirations");
    }

    Set<Workgroup> allGroups = new HashSet<>(expiredGroupMap.keySet());
    allGroups.addAll(upcomingExpirationGroupMap.keySet());

    for (Workgroup w : allGroups) {

      List<String> toAddresses = new ArrayList<>();

      UserAuthorizationService auth = UserAuthorizationService.getInstance();

      String role = w.getLeaderRoleName();

      List<User> leaders = auth.getUsersInRole(role);

      if (leaders != null) {
        for (User s : leaders) {
          if (s.getUsername() != null) {
            toAddresses.add((s.getUsername() + "@jlab.org"));
          }
        }
      }

      List<BeamControlVerification> groupExpiredList = expiredGroupMap.get(w);
      List<BeamControlVerification> groupUpcomingExpirationsList =
          upcomingExpirationGroupMap.get(w);

      String sender = System.getenv("BAM_EMAIL_SENDER");

      String body =
          getExpiredMessageBody(
              proxyServer, null, groupExpiredList, null, groupUpcomingExpirationsList);

      if (!toAddresses.isEmpty()) {
        EmailService emailService = new EmailService();

        String toCsv = toAddresses.get(0);

        for (int i = 1; i < toAddresses.size(); i++) {
          toCsv = toCsv + "," + toAddresses.get(i);
        }

        // Ensure in test env database records JAM_OWNER.WORKGROUP.LEADER_ROLE_NAME point to bogus
        // group else real people will be notified.
        emailService.sendEmail(sender, sender, toCsv, null, subject, body, true);
      }
    }
  }

  @PermitAll
  public void notifyUsersOfExpirationsAndUpcomingExpirations(
      List<BeamDestinationAuthorization> expiredAuthorizationList,
      List<BeamControlVerification> expiredVerificationList,
      List<BeamDestinationAuthorization> upcomingAuthorizationExpirationList,
      List<BeamControlVerification> upcomingVerificationExpirationList) {

    boolean expiredAuth = (expiredAuthorizationList != null && !expiredAuthorizationList.isEmpty());
    boolean expiredVer = (expiredVerificationList != null && !expiredVerificationList.isEmpty());
    boolean upcomingAuth =
        (upcomingAuthorizationExpirationList != null
            && !upcomingAuthorizationExpirationList.isEmpty());
    boolean upcomingVer =
        (upcomingVerificationExpirationList != null
            && !upcomingVerificationExpirationList.isEmpty());

    if (expiredAuth || expiredVer || upcomingAuth || upcomingVer) {

      LOGGER.log(Level.FINEST, "Notifying users");
      String proxyServer = System.getenv("FRONTEND_SERVER_URL");

      try {
        // Admins
        notifyAdmins(
            expiredAuthorizationList,
            expiredVerificationList,
            upcomingAuthorizationExpirationList,
            upcomingVerificationExpirationList,
            proxyServer);

        // Ops
        if (expiredAuth || expiredVer) {
          notifyOps(expiredAuthorizationList, expiredVerificationList, proxyServer);
        }

        // Groups
        if (expiredVer || upcomingVer) {
          notifyGroups(expiredVerificationList, upcomingVerificationExpirationList, proxyServer);
        }

      } catch (MessagingException | NullPointerException | UserFriendlyException e) {
        LOGGER.log(Level.WARNING, "Unable to send email", e);
      }
    } else {
      LOGGER.log(Level.FINEST, "Nothing to notify users about");
    }
  }

  @PermitAll
  public void performExpirationCheck(boolean checkForUpcoming) {
    LOGGER.log(Level.FINEST, "Expiration Check: Director's authorizations...");
    BeamAuthorization auth = beamAuthorizationFacade.findCurrent();
    List<BeamDestinationAuthorization> expiredAuthorizationList = null;

    if (auth != null) {
      expiredAuthorizationList = checkForAuthorizedButExpired(auth);
      if (expiredAuthorizationList != null && !expiredAuthorizationList.isEmpty()) {
        LOGGER.log(Level.FINEST, "Expiration Check: Revoking expired authorization");
        revokeExpiredAuthorizations(expiredAuthorizationList);
      }
    }

    LOGGER.log(Level.FINEST, "Expiration Check: Checking for expired verifications...");
    List<BeamControlVerification> expiredVerificationList =
        checkForVerifiedButExpired(); // only items which are "verified" or "provisionally
    // verified", but need to be "not verified" due to expiration
    if (expiredVerificationList != null && !expiredVerificationList.isEmpty()) {
      LOGGER.log(Level.FINEST, "Expiration Check: Revoking expired verifications...");
      revokeExpiredVerifications(expiredVerificationList);
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

    notifyUsersOfExpirationsAndUpcomingExpirations(
        expiredAuthorizationList,
        expiredVerificationList,
        upcomingAuthorizationExpirationList,
        upcomingVerificationExpirationList);
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
