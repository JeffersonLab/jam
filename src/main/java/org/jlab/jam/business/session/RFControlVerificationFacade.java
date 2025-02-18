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
public class RFControlVerificationFacade extends AbstractFacade<RFControlVerification> {

  private static final Logger LOGGER =
      Logger.getLogger(RFControlVerificationFacade.class.getName());

  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @EJB CreditedControlFacade controlFacade;
  @EJB RFSegmentFacade segmentFacade;
  @EJB RFAuthorizationFacade rfAuthorizationFacade;

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
  public List<RFControlVerification> edit(
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

    if(expirationDate == null && verificationId == 1) {
      throw new UserFriendlyException("expiration date required when status is Verified");
    }

    List<RFControlVerification> downgradeList = new ArrayList<>();

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

      if (downgrade) {
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
      history.setRFControlVerification(verification);
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
      List<RFSegmentAuthorization> expiredAuthorizationList,
      List<RFControlVerification> expiredVerificationList,
      List<RFSegmentAuthorization> upcomingAuthorizationExpirationList,
      List<RFControlVerification> upcomingVerificationExpirationList) {
    StringBuilder builder = new StringBuilder();

    SimpleDateFormat formatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    if (expiredAuthorizationList != null && !expiredAuthorizationList.isEmpty()) {
      builder.append("<h1>--- Expired Director's Authorizations ---</h1>\n");
      for (RFSegmentAuthorization authorization : expiredAuthorizationList) {
        builder.append("</div>\n<div><b>RF Segment:</b> ");
        builder.append(authorization.getSegment().getName());
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

      for (RFControlVerification v : expiredVerificationList) {

        builder.append("<div><b>Credited Control:</b> ");
        builder.append(v.getCreditedControl().getName());
        builder.append("</div>\n<div><b>RF Segment:</b> ");
        builder.append(v.getRFSegment().getName());
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

      for (RFSegmentAuthorization authorization : upcomingAuthorizationExpirationList) {

        builder.append("<div><b>RF Segment:</b> ");
        builder.append(authorization.getSegment().getName());
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

      for (RFControlVerification v : upcomingVerificationExpirationList) {

        builder.append("<div><b>Credited Control:</b> ");
        builder.append(v.getCreditedControl().getName());
        builder.append("</div>\n<div><b>RF Segment:</b> ");
        builder.append(v.getRFSegment().getName());
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
      String proxyServer, List<RFControlVerification> downgradeList) {
    StringBuilder builder = new StringBuilder();

    SimpleDateFormat formatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    RFControlVerification verification = downgradeList.get(0);

    builder.append("<div><b>Credited Control:</b> ");
    builder.append(verification.getCreditedControl().getName());
    builder.append("</div>\n<div><b>RF Segments:</b> ");
    for (RFControlVerification v : downgradeList) {
      builder.append("<div>");
      builder.append(v.getRFSegment().getName());
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

    String subject = System.getenv("JAM_DOWNGRADED_SUBJECT");

    String logbooks = System.getenv("JAM_BOOKS_CSV");

    if (logbooks == null || logbooks.isEmpty()) {
      logbooks = "TLOG";
      LOGGER.log(
          Level.WARNING, "Environment variable 'JAM_BOOKS_CSV' not found, using default TLOG");
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
    String toCsv = System.getenv("JAM_DOWNGRADED_EMAIL_CSV");

    String subject = System.getenv("JAM_DOWNGRADED_SUBJECT");

    EmailService emailService = new EmailService();

    String sender = System.getenv("JAM_EMAIL_SENDER");

    emailService.sendEmail(sender, sender, toCsv, null, subject, body, true);
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
  public List<RFControlVerification> checkForVerifiedButExpired() {
    TypedQuery<RFControlVerification> q =
        em.createQuery(
            "select a from RFControlVerification a join fetch a.creditedControl where a.expirationDate < sysdate and a.rfSegment.active = true and a.verificationStatusId in (1, 50) order by a.creditedControl.weight asc",
            RFControlVerification.class);

    return q.getResultList();
  }

  @PermitAll
  public void revokeExpiredAuthorizations(List<RFSegmentAuthorization> authorizationList) {
    LOGGER.log(Level.FINEST, "I think I've got something authorization-wise to downgrade");
    this.clearDirectorPermissionBySegmentAuthorization(authorizationList);
  }

  @PermitAll
  public void revokeExpiredVerifications(List<RFControlVerification> expiredList) {
    Query q =
        em.createQuery(
            "update RFControlVerification a set a.verificationStatusId = 100, a.comments = 'Expired', a.verifiedBy = null, a.verificationDate = :vDate, a.modifiedDate = :vDate, a.modifiedBy = 'authadm' where a.rfControlVerificationId in :list");

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

    clearDirectorPermissionForExpired(expiredList);
  }

  @PermitAll
  public void clearDirectorPermissionForExpired(List<RFControlVerification> verificationList) {
    clearDirectorPermissionByCreditedControl(verificationList, true);
  }

  @PermitAll
  public void clearDirectorPermissionForDowngrade(List<RFControlVerification> verificationList) {
    clearDirectorPermissionByCreditedControl(verificationList, false);
  }

  private void clearDirectorPermissionByCreditedControl(
      List<RFControlVerification> verificationList, Boolean expiration) {
    String reason = "expiration";

    if (!expiration) {
      reason = "downgrade";
    }

    RFAuthorization rfAuthorization = rfAuthorizationFacade.findCurrent();

    if (rfAuthorization == null) {
      LOGGER.log(Level.INFO, "No current RFAuthorization, so nothing to downgrade");
      return;
    }

    RFAuthorization authClone = rfAuthorization.createAdminClone();
    // authClone.setDestinationAuthorizationList(new ArrayList<>());
    List<RFSegmentAuthorization> newList = new ArrayList<>();

    boolean atLeastOne = false;

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
            destClone.setComments(
                "Permission automatically revoked due to credited control verification " + reason);
            LOGGER.log(Level.FINEST, "Found something to downgrade");
            atLeastOne = true;
            break; // Found a match so revoke and then break out of loop
          }
        }
      }
    }

    if (atLeastOne) {
      em.persist(authClone);
      for (RFSegmentAuthorization da : newList) {
        SegmentAuthorizationPK pk = new SegmentAuthorizationPK();
        pk.setRFSegmentId(da.getSegment().getRFSegmentId());
        pk.setRFAuthorizationId(authClone.getRfAuthorizationId());
        da.setSegmentAuthorizationPK(pk);
        em.persist(da);
      }
    }
  }

  private void clearDirectorPermissionBySegmentAuthorization(
      List<RFSegmentAuthorization> segmentList) {
    RFAuthorization rfAuthorization = rfAuthorizationFacade.findCurrent();

    RFAuthorization authClone = rfAuthorization.createAdminClone();
    // authClone.setDestinationAuthorizationList(new ArrayList<>());
    List<RFSegmentAuthorization> newList = new ArrayList<>();

    boolean atLeastOne = false;

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
          continue; // Already No High Power RF auth so no need to revoke; move on to next
        }
        if (segmentList.contains(auth)) {
          destClone.setHighPowerRf(false);
          destClone.setComments(
              "Permission automatically revoked due to director's authorization expiration");
          LOGGER.log(Level.FINEST, "Found something to downgrade");
          atLeastOne = true;
        }
      }
    }

    if (atLeastOne) {
      em.persist(authClone);
      for (RFSegmentAuthorization da : newList) {
        SegmentAuthorizationPK pk = new SegmentAuthorizationPK();
        pk.setRFSegmentId(da.getSegment().getRFSegmentId());
        pk.setRFAuthorizationId(authClone.getRfAuthorizationId());
        da.setSegmentAuthorizationPK(pk);
        em.persist(da);
      }
    }
  }

  @PermitAll
  public void insertExpiredHistory(
      List<RFControlVerification> verificationList, Date modifiedDate) {
    for (RFControlVerification v : verificationList) {
      RFControlVerificationHistory history = new RFControlVerificationHistory();
      history.setModifiedBy("jam-admin");
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
  public List<RFControlVerification> checkForUpcomingVerificationExpirations() {
    TypedQuery<RFControlVerification> q =
        em.createQuery(
            "select a from RFControlVerification a join fetch a.creditedControl where a.expirationDate >= sysdate and (a.expirationDate - 7) <= sysdate and a.verificationStatusId in (1, 50) and a.rfSegment.active = true order by a.creditedControl.weight asc",
            RFControlVerification.class);

    return q.getResultList();
  }

  private List<RFSegmentAuthorization> checkForUpcomingAuthorizationExpirations(
      RFAuthorization auth) {
    List<RFSegmentAuthorization> upcomingExpirations = new ArrayList<>();

    Date now = new Date();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, 3);
    Date threeDaysFromNow = cal.getTime();

    if (auth.getRFSegmentAuthorizationList() != null) {
      for (RFSegmentAuthorization dest : auth.getRFSegmentAuthorizationList()) {
        if (dest.isHighPowerRf()
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
      List<RFSegmentAuthorization> expiredAuthorizationList,
      List<RFControlVerification> expiredVerificationList,
      List<RFSegmentAuthorization> upcomingAuthorizationExpirationList,
      List<RFControlVerification> upcomingVerificationExpirationList,
      String proxyServer)
      throws MessagingException, UserFriendlyException {
    String toCsv = System.getenv("JAM_UPCOMING_EXPIRATION_EMAIL_CSV");

    String subject = System.getenv("JAM_UPCOMING_EXPIRATION_SUBJECT");

    String body =
        getExpiredMessageBody(
            proxyServer,
            expiredAuthorizationList,
            expiredVerificationList,
            upcomingAuthorizationExpirationList,
            upcomingVerificationExpirationList);

    EmailService emailService = new EmailService();

    String sender = System.getenv("JAM_EMAIL_SENDER");

    emailService.sendEmail(sender, sender, toCsv, null, subject, body, true);
  }

  @PermitAll
  public void notifyOps(
      List<RFSegmentAuthorization> expiredAuthorizationList,
      List<RFControlVerification> expiredVerificationList,
      String proxyServer)
      throws MessagingException, UserFriendlyException {
    String toCsv = System.getenv("JAM_EXPIRED_EMAIL_CSV");

    String subject = System.getenv("JAM_EXPIRED_SUBJECT");

    String body =
        getExpiredMessageBody(
            proxyServer, expiredAuthorizationList, expiredVerificationList, null, null);

    EmailService emailService = new EmailService();

    String sender = System.getenv("JAM_EMAIL_SENDER");

    emailService.sendEmail(sender, sender, toCsv, null, subject, body, true);
    LOGGER.log(Level.FINEST, "notifyOps, toCsv: {0], body: {1}", new Object[] {toCsv, body});
  }

  @PermitAll
  public void notifyGroups(
      List<RFControlVerification> expiredList,
      List<RFControlVerification> upcomingExpirationsList,
      String proxyServer)
      throws MessagingException, UserFriendlyException {
    Map<VerificationTeam, List<RFControlVerification>> expiredGroupMap = new HashMap<>();
    Map<VerificationTeam, List<RFControlVerification>> upcomingExpirationGroupMap = new HashMap<>();

    String subject = System.getenv("JAM_UPCOMING_EXPIRATION_SUBJECT");

    LOGGER.log(Level.FINEST, "Expirations:");
    if (expiredList != null) {
      for (RFControlVerification c : expiredList) {
        LOGGER.log(Level.FINEST, c.toString());
        VerificationTeam verificationTeam = c.getCreditedControl().getVerificationTeam();
        List<RFControlVerification> groupList = expiredGroupMap.get(verificationTeam);
        if (groupList == null) {
          groupList = new ArrayList<>();
          expiredGroupMap.put(verificationTeam, groupList);
        }
        groupList.add(c);
      }
    } else {
      LOGGER.log(Level.FINEST, "No expirations");
    }

    LOGGER.log(Level.FINEST, "Upcoming Expirations:");
    if (upcomingExpirationsList != null) {
      for (RFControlVerification c : upcomingExpirationsList) {
        LOGGER.log(Level.FINEST, c.toString());
        VerificationTeam verificationTeam = c.getCreditedControl().getVerificationTeam();
        List<RFControlVerification> groupList = upcomingExpirationGroupMap.get(verificationTeam);
        if (groupList == null) {
          groupList = new ArrayList<>();
          upcomingExpirationGroupMap.put(verificationTeam, groupList);
        }
        groupList.add(c);
      }
    } else {
      LOGGER.log(Level.FINEST, "No upcoming expirations");
    }

    Set<VerificationTeam> allGroups = new HashSet<>(expiredGroupMap.keySet());
    allGroups.addAll(upcomingExpirationGroupMap.keySet());

    for (VerificationTeam w : allGroups) {

      List<String> toAddresses = new ArrayList<>();

      UserAuthorizationService auth = UserAuthorizationService.getInstance();

      String role = w.getDirectoryRoleName();

      List<User> leaders = auth.getUsersInRole(role);

      if (leaders != null) {
        for (User s : leaders) {
          if (s.getUsername() != null) {
            toAddresses.add((s.getUsername() + "@jlab.org"));
          }
        }
      }

      List<RFControlVerification> groupExpiredList = expiredGroupMap.get(w);
      List<RFControlVerification> groupUpcomingExpirationsList = upcomingExpirationGroupMap.get(w);

      String sender = System.getenv("JAM_EMAIL_SENDER");

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
      List<RFSegmentAuthorization> expiredAuthorizationList,
      List<RFControlVerification> expiredVerificationList,
      List<RFSegmentAuthorization> upcomingAuthorizationExpirationList,
      List<RFControlVerification> upcomingVerificationExpirationList) {

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
    RFAuthorization auth = rfAuthorizationFacade.findCurrent();
    List<RFSegmentAuthorization> expiredAuthorizationList = null;

    if (auth != null) {
      expiredAuthorizationList = checkForAuthorizedButExpired(auth);
      if (expiredAuthorizationList != null && !expiredAuthorizationList.isEmpty()) {
        LOGGER.log(Level.FINEST, "Expiration Check: Revoking expired authorization");
        revokeExpiredAuthorizations(expiredAuthorizationList);
      }
    }

    LOGGER.log(Level.FINEST, "Expiration Check: Checking for expired verifications...");
    List<RFControlVerification> expiredVerificationList =
        checkForVerifiedButExpired(); // only items which are "verified" or "provisionally
    // verified", but need to be "not verified" due to expiration
    if (expiredVerificationList != null && !expiredVerificationList.isEmpty()) {
      LOGGER.log(Level.FINEST, "Expiration Check: Revoking expired verifications...");
      revokeExpiredVerifications(expiredVerificationList);
    }

    List<RFControlVerification> upcomingVerificationExpirationList = null;
    List<RFSegmentAuthorization> upcomingAuthorizationExpirationList = null;
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
