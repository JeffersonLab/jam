package org.jlab.jam.business.session;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.annotation.security.PermitAll;
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
import org.jlab.jam.persistence.view.*;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.service.EmailService;
import org.jlab.smoothness.business.service.UserAuthorizationService;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.view.User;

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

  public static final String LINK_FOOTER;

  static {
    String proxyServer = System.getenv("FRONTEND_SERVER_URL");

    LINK_FOOTER =
        "<br/><b><span style=\"color: red;\">Always check the Authorization web application for the latest status:</span></b> <a href=\""
            + proxyServer
            + "/jam\">"
            + "JLab Authorization Manager</a>";
  }

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
  public void sendRFVerifierDowngradeEmail(RFAuthorization auth, File screenshot) {
    if (auth != null) {
      sendWatcherAuthorizationUpdateEmail(auth.getFacility(), OperationsType.RF, screenshot);

      sendAdminAndManagerAuthorizationUpdateEmail(
          auth.getFacility(), OperationsType.RF, screenshot);
    }
  }

  // Verifier downgrade event (Beam)
  @PermitAll
  public void sendBeamVerifierDowngradeEmail(BeamAuthorization auth, File screenshot) {
    if (auth != null) {
      sendWatcherAuthorizationUpdateEmail(auth.getFacility(), OperationsType.BEAM, screenshot);

      sendAdminAndManagerAuthorizationUpdateEmail(
          auth.getFacility(), OperationsType.BEAM, screenshot);
    }
  }

  // AUTO_EXPIRE event
  @PermitAll
  public void sendRFExpirationEmails(RFExpirationEvent event, File screenshot) {
    if (event != null) {
      if (event.getAuthorization() != null) {
        RFAuthorization auth = event.getAuthorization();
        sendWatcherAuthorizationUpdateEmail(auth.getFacility(), OperationsType.RF, screenshot);

        sendAdminAndManagerAuthorizationUpdateEmail(
            auth.getFacility(), OperationsType.RF, screenshot);

        List<TeamExpirationEvent> teamEventList = getTeamExpirationEventList(event);
        for (TeamExpirationEvent teamEvent : teamEventList) {
          sendVerificationTeamsUpdateDueExpirationEmail(
              auth.getFacility(), OperationsType.RF, screenshot, teamEvent);
        }
      }
    }
  }

  @PermitAll
  public void sendBeamExpirationEmails(BeamExpirationEvent event, File screenshot) {
    if (event != null) {
      if (event.getAuthorization() != null) {
        BeamAuthorization auth = event.getAuthorization();
        sendWatcherAuthorizationUpdateEmail(auth.getFacility(), OperationsType.BEAM, screenshot);

        sendAdminAndManagerAuthorizationUpdateEmail(
            auth.getFacility(), OperationsType.BEAM, screenshot);

        List<TeamExpirationEvent> teamEventList = getTeamExpirationEventList(event);
        for (TeamExpirationEvent teamEvent : teamEventList) {
          sendVerificationTeamsUpdateDueExpirationEmail(
              auth.getFacility(), OperationsType.BEAM, screenshot, teamEvent);
        }
      }
    }
  }

  private List<TeamExpirationEvent> getTeamExpirationEventList(RFExpirationEvent rfEvent) {
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

    return new ArrayList<>(teamEventMap.values());
  }

  private List<TeamExpirationEvent> getTeamExpirationEventList(BeamExpirationEvent beamEvent) {
    Map<VerificationTeam, TeamExpirationEvent> teamEventMap = new HashMap<>();

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

    return new ArrayList<>(teamEventMap.values());
  }

  private List<TeamUpcoming> getTeamUpcoming(FacilityUpcomingExpiration upcoming) {
    Map<VerificationTeam, TeamUpcoming> teamMap = new HashMap<>();

    if (upcoming != null) {
      if (upcoming.getUpcomingRFVerificationExpirationList() != null) {
        for (RFControlVerification verification :
            upcoming.getUpcomingRFVerificationExpirationList()) {
          VerificationTeam team = verification.getCreditedControl().getVerificationTeam();
          TeamUpcoming tup = teamMap.get(team);
          if (tup == null) {
            tup = new TeamUpcoming(team);
            teamMap.put(team, tup);
          }
          tup.getRfUpcomingVerificationExpirationList().add(verification);
        }
      }

      if (upcoming.getUpcomingBeamVerificationExpirationList() != null) {
        for (BeamControlVerification verification :
            upcoming.getUpcomingBeamVerificationExpirationList()) {
          VerificationTeam team = verification.getCreditedControl().getVerificationTeam();
          TeamUpcoming tup = teamMap.get(team);
          if (tup == null) {
            tup = new TeamUpcoming(team);
            teamMap.put(team, tup);
          }
          tup.getBeamUpcomingVerificationExpirationList().add(verification);
        }
      }
    }

    return new ArrayList<>(teamMap.values());
  }

  private void sendUpcomingToAdminsAndFacilityManager(FacilityUpcomingExpiration upcoming) {
    try {
      EmailService emailService = new EmailService();

      String sender = System.getenv("JAM_EMAIL_SENDER");

      if (sender == null) {
        sender = "jam@jlab.org";
      }

      boolean testing = false;
      String adminRole = "jam-admin";
      String testingStr = System.getenv("JAM_EMAIL_TESTING");
      if (testingStr != null && testingStr.equals("true")) {
        testing = true;
        LOGGER.log(Level.INFO, "JAM_EMAIL_TESTING=true (using testlead role)");
        adminRole = "testlead";
      }

      String subject = "JAM: " + upcoming.getFacility().getName() + " Upcoming Expiration Notice";

      String body = getUpcomingAdminAndFacilityManagerBody(upcoming);

      if (body == null) {
        return;
      }

      Set<String> addressList = new HashSet<>();

      if (upcoming.getFacility().getManagerUsername() != null && !testing) {
        addressList.add(upcoming.getFacility().getManagerUsername() + EMAIL_DOMAIN);
      }

      UserAuthorizationService auth = UserAuthorizationService.getInstance();
      List<User> userList = auth.getUsersInRole(adminRole);

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

  @PermitAll
  public void sendWatcherAuthorizationUpdateEmail(
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

      String subject =
          "JAM: " + facility.getName() + " " + type.getLabel() + " Authorization Updated";

      String body =
          "<img src=\"cid:screenshot\"/>"
              + LINK_FOOTER
              + "<br/><i>You are receiving this email because you are configured as a JAM Watcher</i>";

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

  @PermitAll
  public void sendAdminAndManagerAuthorizationUpdateEmail(
      Facility facility, OperationsType type, File screenshot) {
    try {
      boolean testing = false;
      String adminRole = "jam-admin";
      String testingStr = System.getenv("JAM_EMAIL_TESTING");
      if (testingStr != null && testingStr.equals("true")) {
        testing = true;
        LOGGER.log(Level.INFO, "JAM_EMAIL_TESTING=true (using testlead role)");
        adminRole = "testlead";
      }

      Set<String> addressList = new HashSet<>();

      if (facility.getManagerUsername() != null && !testing) {
        addressList.add(facility.getManagerUsername() + EMAIL_DOMAIN);
      }

      UserAuthorizationService auth = UserAuthorizationService.getInstance();

      List<User> userList = auth.getUsersInRole(adminRole);

      if (userList != null) {
        for (User user : userList) {
          addressList.add(user.getEmail());
        }
      }

      if (addressList == null || addressList.isEmpty()) {
        LOGGER.log(
            Level.WARNING,
            "No Admins or Managers configured for facility "
                + facility.getName()
                + " and OperationsType "
                + type.name()
                + ", aborting");
        return;
      }

      String subject =
          "JAM: " + facility.getName() + " " + type.getLabel() + " Authorization Updated";

      String body =
          "<img src=\"cid:screenshot\"/>"
              + LINK_FOOTER
              + "<br/><i>You are receiving this email because you are configured as a JAM Admin/Manager</i>";

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

      String toCsv = IOUtil.toCsv(addressList.toArray());

      sendEmailMultipart(sender, sender, toCsv, null, subject, multipart);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  @PermitAll
  public void sendVerificationTeamsUpdateDueExpirationEmail(
      Facility facility,
      OperationsType type,
      File screenshot,
      TeamExpirationEvent teamExpirationEvent) {
    try {
      Set<String> addressList = new HashSet<>();
      List<User> userList;

      UserAuthorizationService auth = UserAuthorizationService.getInstance();

      String testingStr = System.getenv("JAM_EMAIL_TESTING");
      if (testingStr != null && testingStr.equals("true")) {
        LOGGER.log(Level.INFO, "JAM_EMAIL_TESTING=true (using testlead role)");
        userList = auth.getUsersInRole("testlead");
      } else {
        userList = auth.getUsersInRole(teamExpirationEvent.getTeam().getDirectoryRoleName());
      }

      if (userList != null) {
        for (User user : userList) {
          addressList.add(user.getEmail());
        }
      }

      if (addressList == null || addressList.isEmpty()) {
        LOGGER.log(
            Level.WARNING,
            "No members configured for verification team "
                + teamExpirationEvent.getTeam().getName()
                + ", aborting");
        return;
      }

      String subject =
          "JAM: "
              + facility.getName()
              + " "
              + type.getLabel()
              + " Authorization Reduced due to Expiration";

      String body =
          "<img src=\"cid:screenshot\"/>"
              + LINK_FOOTER
              + "<br/><i>You are receiving this email because you are configured as a JAM Control Verifier</i>";

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

      String toCsv = IOUtil.toCsv(addressList.toArray());

      sendEmailMultipart(sender, sender, toCsv, null, subject, multipart);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  @PermitAll
  public void sendUpcomingEmails(FacilityUpcomingExpiration upcoming) {
    sendUpcomingToAdminsAndFacilityManager(upcoming);

    List<TeamUpcoming> teamUpcomingList = getTeamUpcoming(upcoming);
    for (TeamUpcoming teamUpcoming : teamUpcomingList) {
      sendUpcomingToVerificationTeam(upcoming.getFacility(), teamUpcoming);
    }
  }

  private void sendUpcomingToVerificationTeam(Facility facility, TeamUpcoming upcoming) {
    try {
      EmailService emailService = new EmailService();

      String sender = System.getenv("JAM_EMAIL_SENDER");

      if (sender == null) {
        sender = "jam@jlab.org";
      }

      String subject = "JAM: " + facility.getName() + " Upcoming Expiration Notice";

      String body = getUpcomingTeamBody(upcoming);

      if (body == null) {
        System.err.println("Skipping team email because nothing to report");
        return;
      }

      Set<String> addressList = new HashSet<>();

      UserAuthorizationService auth = UserAuthorizationService.getInstance();

      List<User> userList;

      String testingStr = System.getenv("JAM_EMAIL_TESTING");
      if (testingStr != null && testingStr.equals("true")) {
        LOGGER.log(Level.INFO, "JAM_EMAIL_TESTING=true (using testlead role)");
        userList = auth.getUsersInRole("testlead");
      } else {
        VerificationTeam team = upcoming.getTeam();
        userList = auth.getUsersInRole(team.getDirectoryRoleName());
      }

      if (userList != null) {
        for (User s : userList) {
          if (s.getUsername() != null) {
            addressList.add((s.getUsername() + EMAIL_DOMAIN));
          }
        }
      }

      String toCsv = null;

      if (!addressList.isEmpty()) {
        toCsv = IOUtil.toCsv(addressList.toArray());
      }

      if (toCsv != null) {
        emailService.sendEmail(sender, sender, toCsv, null, subject, body, true);
      } else {
        LOGGER.warning("Skipping team email: No addresses provided");
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private String getUpcomingAdminAndFacilityManagerBody(FacilityUpcomingExpiration upcoming) {

    SimpleDateFormat formatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    boolean somethingToReport = false;
    String body = "";

    if (upcoming.getUpcomingRFAuthorizationExpirationList() != null
        && !upcoming.getUpcomingRFAuthorizationExpirationList().isEmpty()) {
      somethingToReport = true;
      body = body + "<h2>RF Operations Authorizations</h2>\n";
      body = body + "<ul>\n";
      for (RFSegmentAuthorization auth : upcoming.getUpcomingRFAuthorizationExpirationList()) {
        body =
            body
                + "<li>"
                + IOUtil.escapeXml(auth.getSegment().getName())
                + " - "
                + formatter.format(auth.getExpirationDate())
                + "</li>\n";
      }
      body = body + "</ul>\n";
    }

    if (upcoming.getUpcomingRFVerificationExpirationList() != null
        && !upcoming.getUpcomingRFVerificationExpirationList().isEmpty()) {
      somethingToReport = true;
      body = body + "<h2>RF Control Verifications</h2>\n";
      body = body + "<ul>\n";
      for (RFControlVerification v : upcoming.getUpcomingRFVerificationExpirationList()) {
        body =
            body
                + "<li>"
                + IOUtil.escapeXml(v.getRFSegment().getName())
                + " - "
                + formatter.format(v.getExpirationDate())
                + "</li>\n";
      }
      body = body + "</ul>\n";
    }

    if (upcoming.getUpcomingBeamAuthorizationExpirationList() != null
        && !upcoming.getUpcomingBeamAuthorizationExpirationList().isEmpty()) {
      somethingToReport = true;
      body = body + "<h2>Beam Operations Authorizations</h2>\n";
      body = body + "<ul>\n";
      for (BeamDestinationAuthorization auth :
          upcoming.getUpcomingBeamAuthorizationExpirationList()) {
        body =
            body
                + "<li>"
                + IOUtil.escapeXml(auth.getDestination().getName())
                + " - "
                + formatter.format(auth.getExpirationDate())
                + "</li>\n";
      }
      body = body + "</ul>\n";
    }

    if (upcoming.getUpcomingBeamVerificationExpirationList() != null
        && !upcoming.getUpcomingBeamVerificationExpirationList().isEmpty()) {
      somethingToReport = true;
      body = body + "<h2>Beam Control Verifications</h2>\n";
      body = body + "<ul>\n";
      for (BeamControlVerification v : upcoming.getUpcomingBeamVerificationExpirationList()) {
        body =
            body
                + "<li>"
                + IOUtil.escapeXml(v.getBeamDestination().getName())
                + " - "
                + formatter.format(v.getExpirationDate())
                + "</li>\n";
      }
      body = body + "</ul>\n";
    }

    if (somethingToReport) {
      body =
          body
              + LINK_FOOTER
              + "<br/><i>You are receiving this email because you are configured as a JAM Admin/Manager</i>";
    } else {
      body = null;
    }

    return body;
  }

  private String getUpcomingTeamBody(TeamUpcoming upcoming) {

    SimpleDateFormat formatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    boolean somethingToReport = false;
    String body = "";

    if (upcoming.getRfUpcomingVerificationExpirationList() != null
        && !upcoming.getRfUpcomingVerificationExpirationList().isEmpty()) {
      somethingToReport = true;
      body = body + "<h2>RF Control Verifications</h2>\n";
      body = body + "<ul>\n";
      for (RFControlVerification v : upcoming.getRfUpcomingVerificationExpirationList()) {
        body =
            body
                + "<li>"
                + IOUtil.escapeXml(v.getRFSegment().getName())
                + " - "
                + formatter.format(v.getExpirationDate())
                + "</li>\n";
      }
      body = body + "</ul>\n";
    }

    if (upcoming.getBeamUpcomingVerificationExpirationList() != null
        && !upcoming.getBeamUpcomingVerificationExpirationList().isEmpty()) {
      somethingToReport = true;
      body = body + "<h2>Beam Control Verifications</h2>\n";
      body = body + "<ul>\n";
      for (BeamControlVerification v : upcoming.getBeamUpcomingVerificationExpirationList()) {
        body =
            body
                + "<li>"
                + IOUtil.escapeXml(v.getBeamDestination().getName())
                + " - "
                + formatter.format(v.getExpirationDate())
                + "</li>\n";
      }
      body = body + "</ul>\n";
    }

    if (somethingToReport) {
      body =
          body
              + LINK_FOOTER
              + "<br/><i>You are receiving this email because you are configured as a JAM Control Verifier</i>";
    } else {
      body = null;
    }

    return body;
  }
}
