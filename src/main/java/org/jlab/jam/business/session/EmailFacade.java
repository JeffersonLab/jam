package org.jlab.jam.business.session;

import java.io.File;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.annotation.security.PermitAll;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.*;
import javax.mail.internet.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jlab.jam.persistence.entity.*;
import org.jlab.jam.persistence.enumeration.OperationsType;
import org.jlab.jam.persistence.view.BeamExpirationEvent;
import org.jlab.jam.persistence.view.FacilityExpirationEvent;
import org.jlab.jam.persistence.view.RFExpirationEvent;
import org.jlab.jam.persistence.view.TeamExpirationEvent;
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
public class EmailFacade extends AbstractFacade<VerificationTeam> {

  private static final Logger LOGGER = Logger.getLogger(EmailFacade.class.getName());

  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  private final Session mailSession;

  @EJB WatcherFacade watcherFacade;
  @EJB RFAuthorizationFacade rfAuthorizationFacade;
  @EJB BeamAuthorizationFacade beamAuthorizationFacade;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public EmailFacade() {
    super(VerificationTeam.class);

    try {
      this.mailSession = (Session) (new InitialContext()).lookup("mail/jlab");
    } catch (NamingException e) {
      throw new RuntimeException("Unable to obtain email session", e);
    }
  }

  public void sendEmailMultipart(
      String sender, String from, String toCsv, String ccCsv, String subject, Multipart multipart)
      throws UserFriendlyException {
    try {
      Address senderAddress = new InternetAddress(sender);
      Address fromAddress = new InternetAddress(from);
      if (sender != null && !sender.isEmpty()) {
        if (from != null && !from.isEmpty()) {
          if (toCsv != null && !toCsv.isEmpty()) {
            if (subject != null && !subject.isEmpty()) {
              if (multipart != null && multipart.getCount() > 0) {
                Address[] toAddresses = EmailService.csvToAddressArray(toCsv);
                Address[] ccAddresses = EmailService.csvToAddressArray(ccCsv);
                this.doSendMultipart(
                    senderAddress, fromAddress, toAddresses, ccAddresses, subject, multipart);
              } else {
                throw new UserFriendlyException("multipart content must not be empty");
              }
            } else {
              throw new UserFriendlyException("subject must not be empty");
            }
          } else {
            throw new UserFriendlyException("to email address must not be empty");
          }
        } else {
          throw new UserFriendlyException("from email address must not be empty");
        }
      } else {
        throw new UserFriendlyException("sender email address must not be empty");
      }
    } catch (AddressException e) {
      throw new IllegalArgumentException("Invalid address", e);
    } catch (MessagingException e) {
      throw new IllegalArgumentException("Unable to send email", e);
    }
  }

  private void doSendMultipart(
      Address sender,
      Address from,
      Address[] toAddresses,
      Address[] ccAddresses,
      String subject,
      Multipart multipart)
      throws MessagingException {
    MimeMessage message = new MimeMessage(this.mailSession);
    message.setSender(sender);
    message.setFrom(from);
    message.setRecipients(Message.RecipientType.TO, toAddresses);
    message.setRecipients(Message.RecipientType.CC, ccAddresses);
    message.setSubject(subject);
    message.setContent(multipart);

    message.saveChanges();
    Transport tr = this.mailSession.getTransport();
    tr.connect();
    tr.sendMessage(message, message.getAllRecipients());
    tr.close();
  }

  public static final String EMAIL_DOMAIN = "@jlab.org";

  // Verifier downgrade event (RF)
  @PermitAll
  @Asynchronous
  public void sendAsyncRFVerifierDowngradeEmail(
      Facility facility, List<RFControlVerification> downgradeList) {}

  // Verifier downgrade event (Beam)
  @PermitAll
  @Asynchronous
  public void sendAsyncBeamVerifierDowngradeEmail(
      Facility facility, List<BeamControlVerification> downgradeList) {}

  // Authorizer update event
  @PermitAll
  public void sendAuthorizerChangeEmail(
      OperationsType type, BigInteger authorizationId, File screenshot) {
    if (OperationsType.RF.equals(type)) {
      RFAuthorization auth = rfAuthorizationFacade.find(authorizationId);
      sendAuthorizationUpdateEmail(auth.getFacility(), type, screenshot);
    } else {
      BeamAuthorization auth = beamAuthorizationFacade.find(authorizationId);
      sendBeamAuthorizationUpdateEmail(auth, screenshot);
    }
  }

  // Scheduled Nightly AUTO_EXPIRE event
  @PermitAll
  @Asynchronous
  public void sendAsyncExpirationEmails(Map<Facility, FacilityExpirationEvent> facilityMap) {}

  // AUTO_EXPIRE event
  @PermitAll
  @Asynchronous
  public void sendAsyncExpirationEmails(FacilityExpirationEvent event) {
    Facility facility = event.getFacility();
    RFExpirationEvent rfEvent = event.getRfEvent();
    BeamExpirationEvent beamEvent = event.getBeamEvent();

    boolean rfExpiredAuthorization =
        (rfEvent != null
            && rfEvent.getExpiredAuthorizationList() != null
            && !rfEvent.getExpiredAuthorizationList().isEmpty());
    boolean rfExpiredVerification =
        (rfEvent != null
            && rfEvent.getExpiredVerificationList() != null
            && !rfEvent.getExpiredVerificationList().isEmpty());
    boolean rfUpcomingAuthorization =
        (rfEvent != null
            && rfEvent.getUpcomingAuthorizationExpirationList() != null
            && !rfEvent.getUpcomingAuthorizationExpirationList().isEmpty());
    boolean rfUpcomingVerification =
        (rfEvent != null
            && rfEvent.getUpcomingVerificationExpirationList() != null
            && !rfEvent.getUpcomingVerificationExpirationList().isEmpty());

    boolean beamExpiredAuthorization =
        (beamEvent != null
            && beamEvent.getExpiredAuthorizationList() != null
            && !beamEvent.getExpiredAuthorizationList().isEmpty());
    boolean beamExpiredVerification =
        (beamEvent != null
            && beamEvent.getExpiredVerificationList() != null
            && !beamEvent.getExpiredVerificationList().isEmpty());
    boolean beamUpcomingAuthorization =
        (beamEvent != null
            && beamEvent.getUpcomingAuthorizationExpirationList() != null
            && !beamEvent.getUpcomingAuthorizationExpirationList().isEmpty());
    boolean beamUpcomingVerification =
        (beamEvent != null
            && beamEvent.getUpcomingVerificationExpirationList() != null
            && !beamEvent.getUpcomingVerificationExpirationList().isEmpty());

    if (rfExpiredAuthorization
        || rfUpcomingAuthorization
        || rfExpiredVerification
        || rfUpcomingVerification
        || beamExpiredAuthorization
        || beamUpcomingAuthorization
        || beamExpiredVerification
        || beamUpcomingVerification) {
      notifyAdminsAndFacilityManager(facility, rfEvent, beamEvent);

      // Watchers don't get upcoming and don't get verification events
      if (rfExpiredAuthorization || beamExpiredAuthorization) {
        notifyWatchers(facility, rfEvent, beamEvent);
      }

      // Verification Teams are notified of their own verification expirations and upcoming
      if (rfExpiredVerification
          || beamExpiredVerification
          || rfUpcomingVerification
          || beamUpcomingVerification) {
        notifyVerificationTeams(facility, rfEvent, beamEvent);
      }
    }
  }

  @PermitAll
  @Asynchronous
  public void sendAsyncExpirationEmails(
      Map<Facility, RFExpirationEvent> rfMap, Map<Facility, BeamExpirationEvent> beamMap) {}

  private void notifyVerificationTeams(
      Facility facility, RFExpirationEvent rfEvent, BeamExpirationEvent beamEvent) {
    try {
      List<TeamExpirationEvent> teamEventList = getTeamExpirationEventList(rfEvent, beamEvent);

      String sender = System.getenv("JAM_EMAIL_SENDER");

      if (sender == null) {
        sender = "jam@jlab.org";
      }

      String subject = facility.getName() + " Expiration Notice";

      UserAuthorizationService auth = UserAuthorizationService.getInstance();

      for (TeamExpirationEvent event : teamEventList) {
        VerificationTeam team = event.getTeam();
        String role = team.getDirectoryRoleName();

        List<User> members = auth.getUsersInRole(role);

        List<String> addressList = new ArrayList<>();

        if (members != null) {
          for (User s : members) {
            if (s.getUsername() != null) {
              addressList.add((s.getUsername() + EMAIL_DOMAIN));
            }
          }
        }

        String body = getVerificationTeamBody(event);

        String toCsv = null;

        if (!addressList.isEmpty()) {
          toCsv = IOUtil.toCsv(addressList.toArray());
        }

        if (toCsv != null) {
          EmailService emailService = new EmailService();

          emailService.sendEmail(sender, sender, toCsv, null, subject, body, true);
        } else {
          LOGGER.warning("Skipping verification team email: No addresses provided");
        }
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private List<TeamExpirationEvent> getTeamExpirationEventList(
      RFExpirationEvent rfEvent, BeamExpirationEvent beamEvent) {
    Map<VerificationTeam, TeamExpirationEvent> teamEventMap = new HashMap<>();

    if (rfEvent != null && rfEvent.getExpiredVerificationList() != null) {
      for (RFControlVerification c : rfEvent.getExpiredVerificationList()) {
        VerificationTeam team = c.getCreditedControl().getVerificationTeam();
        TeamExpirationEvent event = teamEventMap.get(team);
        if (event == null) {
          event = new TeamExpirationEvent(team);
          teamEventMap.put(team, event);
        }
        event.getRfExpiredVerificationList().add(c);
      }
    }

    if (rfEvent != null && rfEvent.getUpcomingVerificationExpirationList() != null) {
      for (RFControlVerification c : rfEvent.getExpiredVerificationList()) {
        VerificationTeam team = c.getCreditedControl().getVerificationTeam();
        TeamExpirationEvent event = teamEventMap.get(team);
        if (event == null) {
          event = new TeamExpirationEvent(team);
          teamEventMap.put(team, event);
        }
        event.getRfUpcomingVerificationExpirationList().add(c);
      }
    }

    if (beamEvent != null && beamEvent.getExpiredVerificationList() != null) {
      for (BeamControlVerification c : beamEvent.getExpiredVerificationList()) {
        VerificationTeam team = c.getCreditedControl().getVerificationTeam();
        TeamExpirationEvent event = teamEventMap.get(team);
        if (event == null) {
          event = new TeamExpirationEvent(team);
          teamEventMap.put(team, event);
        }
        event.getBeamExpiredVerificationList().add(c);
      }
    }

    if (beamEvent != null && beamEvent.getUpcomingVerificationExpirationList() != null) {
      for (BeamControlVerification c : beamEvent.getExpiredVerificationList()) {
        VerificationTeam team = c.getCreditedControl().getVerificationTeam();
        TeamExpirationEvent event = teamEventMap.get(team);
        if (event == null) {
          event = new TeamExpirationEvent(team);
          teamEventMap.put(team, event);
        }
        event.getBeamUpcomingVerificationExpirationList().add(c);
      }
    }

    return new ArrayList<>(teamEventMap.values());
  }

  private void notifyWatchers(
      Facility facility, RFExpirationEvent rfEvent, BeamExpirationEvent beamEvent) {
    try {
      EmailService emailService = new EmailService();

      String sender = System.getenv("JAM_EMAIL_SENDER");

      if (sender == null) {
        sender = "jam@jlab.org";
      }

      String subject = facility.getName() + " Expiration Notice";

      String body = getWatcherBody(facility, rfEvent, beamEvent);

      List<String> addressList = new ArrayList<>();

      // TODO: Some watchers only want a particular OperationsType
      // Ideally we break this into three emails: JUST RF, JUST BEAM, BOTH.
      // I suspect most watchers will be BOTH and they'll appreciate a combined email
      List<Watcher> watcherList = watcherFacade.filterList(facility, null, null);

      for (Watcher watcher : watcherList) {
        addressList.add(watcher.getWatcherPK().getUsername() + EMAIL_DOMAIN);
      }

      String toCsv = null;

      if (!addressList.isEmpty()) {
        toCsv = IOUtil.toCsv(addressList.toArray());
      }

      if (toCsv != null) {
        emailService.sendEmail(sender, sender, toCsv, null, subject, body, true);
      } else {
        LOGGER.warning("Skipping watcher email: No addresses provided");
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private void notifyAdminsAndFacilityManager(
      Facility facility, RFExpirationEvent rfEvent, BeamExpirationEvent beamEvent) {
    try {
      EmailService emailService = new EmailService();

      String sender = System.getenv("JAM_EMAIL_SENDER");

      if (sender == null) {
        sender = "jam@jlab.org";
      }

      boolean testing = false;
      String testingStr = System.getenv("JAM_EMAIL_TESTING");
      if (testingStr != null && testingStr.equals("true")) {
        testing = true;
      }

      String subject = facility.getName() + " Expiration Notice";

      String body = getAdminAndFacilityManagerBody(facility, rfEvent, beamEvent);

      List<String> addressList = new ArrayList<>();

      if (facility.getManagerUsername() != null && !testing) {
        addressList.add(facility.getManagerUsername() + EMAIL_DOMAIN);
      }

      UserAuthorizationService auth = UserAuthorizationService.getInstance();
      List<User> userList = auth.getUsersInRole("jam-admin");

      if (userList != null) {
        for (User user : userList) {
          addressList.add(user.getEmail());
        }
      }

      String toCsv = null;

      if (!addressList.isEmpty()) {
        toCsv = IOUtil.toCsv(addressList.toArray());
      }

      if (toCsv != null) {
        emailService.sendEmail(sender, sender, toCsv, null, subject, body, true);
      } else {
        LOGGER.warning("Skipping admin/manager email: No addresses provided");
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private void sendAuthorizationUpdateEmail(
      Facility facility, OperationsType type, File screenshot) {
    try {
      List<Watcher> watcherList = watcherFacade.filterList(facility, type, null);

      if (watcherList == null || watcherList.isEmpty()) {
        LOGGER.log(
            Level.WARNING,
            "No Watchers configured for facility "
                + facility.getName()
                + " and OperationsType "
                + type.name()
                + ", aborting");
        return;
      }

      String subject = facility.getName() + " " + type.getLabel() + " Authorization Updated";

      String proxyServer = System.getenv("FRONTEND_SERVER_URL");

      String body =
          "<img src=\"cid:screenshot\"><br/><b><span style=\"color: red;\">Always check the Authorization web application for the latest status:</span></b> <a href=\""
              + proxyServer
              + "/jam\">"
              + "JLab Authorization Manager</a>";

      Multipart multipart = new MimeMultipart("related");

      MimeBodyPart htmlPart = new MimeBodyPart();
      htmlPart.setContent(body, "text/html");
      multipart.addBodyPart(htmlPart);

      MimeBodyPart imagePart = new MimeBodyPart();
      DataSource ds = new FileDataSource(screenshot);
      imagePart.setDataHandler(new DataHandler(ds));
      imagePart.addHeader("Content-ID", "<screenshot>");
      imagePart.addHeader("Content-Type", "image/png");
      multipart.addBodyPart(imagePart);

      String sender = System.getenv("JAM_EMAIL_SENDER");

      if (sender == null) {
        sender = "jam@jlab.org";
      }

      String toCsv = "";

      if (watcherList.size() > 0) {
        Watcher watcher = watcherList.get(0);
        String username = watcher.getWatcherPK().getUsername();
        String address = username + EMAIL_DOMAIN;
        toCsv += address;
      }

      for (int i = 1; i < watcherList.size(); i++) {
        Watcher watcher = watcherList.get(i);
        String username = watcher.getWatcherPK().getUsername();
        String address = username + EMAIL_DOMAIN;
        toCsv += "," + address;
      }

      sendEmailMultipart(sender, sender, toCsv, null, subject, multipart);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public void sendBeamAuthorizationUpdateEmail(BeamAuthorization auth, File screenshot) {
    try {
      Facility facility = auth.getFacility();
      OperationsType type = OperationsType.BEAM;

      List<Watcher> watcherList = watcherFacade.filterList(facility, type, null);

      if (watcherList == null || watcherList.isEmpty()) {
        LOGGER.log(
            Level.WARNING,
            "No Watchers configured for facility "
                + facility.getName()
                + " and OperationsType "
                + type.name()
                + ", aborting");
        return;
      }

      String subject = System.getenv("JAM_PERMISSIONS_SUBJECT");

      if (subject == null) {
        subject = "New Authorization";
        LOGGER.log(Level.WARNING, "No JAM_PERMISSIONS_SUBJECT configured");
      }

      String proxyServer = System.getenv("FRONTEND_SERVER_URL");

      String body = "<a href=\"" + proxyServer + "/jam\">" + proxyServer + "/jam</a>";

      body = body + "\n\n<p>Notes: " + auth.getComments() + "</p>";

      String sender = System.getenv("JAM_EMAIL_SENDER");

      if (sender == null) {
        sender = "jam@jlab.org";
      }

      String toCsv = "";

      if (watcherList.size() > 0) {
        Watcher watcher = watcherList.get(0);
        String username = watcher.getWatcherPK().getUsername();
        String address = username + EMAIL_DOMAIN;
        toCsv += address;
      }

      for (int i = 1; i < watcherList.size(); i++) {
        Watcher watcher = watcherList.get(i);
        String username = watcher.getWatcherPK().getUsername();
        String address = username + EMAIL_DOMAIN;
        toCsv += "," + address;
      }

      EmailService emailService = new EmailService();

      emailService.sendEmail(sender, sender, toCsv, null, subject, body, true);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public String getRFVerificationDowngradeBody(List<RFControlVerification> downgradeList) {
    String proxyServer = System.getenv("FRONTEND_SERVER_URL");

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

  public String getBeamVerificationDowngradedMessageBody(
      List<BeamControlVerification> downgradeList) {
    String proxyServer = System.getenv("FRONTEND_SERVER_URL");
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

  private String getAdminAndFacilityManagerBody(
      Facility facility, RFExpirationEvent rfEvent, BeamExpirationEvent beamEvent) {
    String proxyServer = System.getenv("FRONTEND_SERVER_URL");

    if (proxyServer == null) {
      proxyServer = "localhost";
    }

    String body = "Testing";

    return body;
  }

  private String getWatcherBody(
      Facility facility, RFExpirationEvent rfEvent, BeamExpirationEvent beamEvent) {
    String proxyServer = System.getenv("FRONTEND_SERVER_URL");

    if (proxyServer == null) {
      proxyServer = "localhost";
    }

    String body = "Testing";

    return body;
  }

  private String getVerificationTeamBody(TeamExpirationEvent event) {
    String proxyServer = System.getenv("FRONTEND_SERVER_URL");

    if (proxyServer == null) {
      proxyServer = "localhost";
    }

    String body = "Testing";

    return body;
  }

  @PermitAll
  public String getRFExpiredMessageBody(
      List<RFSegmentAuthorization> expiredAuthorizationList,
      List<RFControlVerification> expiredVerificationList,
      List<RFSegmentAuthorization> upcomingAuthorizationExpirationList,
      List<RFControlVerification> upcomingVerificationExpirationList) {
    String proxyServer = System.getenv("FRONTEND_SERVER_URL");
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

  public String getBeamExpiredMessageBody(
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
}
