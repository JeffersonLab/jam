package org.jlab.jam.business.session;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jlab.jam.persistence.entity.*;
import org.jlab.jam.persistence.enumeration.OperationsType;
import org.jlab.jam.persistence.view.BeamExpirationEvent;
import org.jlab.jam.persistence.view.FacilityExpirationEvent;
import org.jlab.jam.persistence.view.RFExpirationEvent;
import org.jlab.jam.persistence.view.TeamExpirationEvent;
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

  @EJB WatcherFacade watcherFacade;
  @EJB RFAuthorizationFacade rfAuthorizationFacade;
  @EJB BeamAuthorizationFacade beamAuthorizationFacade;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public EmailFacade() {
    super(VerificationTeam.class);
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
  @Asynchronous
  public void sendAsyncAuthorizerChangeEmail(OperationsType type, BigInteger authorizationId) {
    if (OperationsType.RF.equals(type)) {
      RFAuthorization auth = rfAuthorizationFacade.find(authorizationId);
      sendRFAuthorizationUpdateEmail(auth);
    } else {
      BeamAuthorization auth = beamAuthorizationFacade.find(authorizationId);
      sendBeamAuthorizationUpdateEmail(auth);
    }
  }

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

      EmailService emailService = new EmailService();

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

  public void sendRFAuthorizationUpdateEmail(RFAuthorization auth) {
    try {
      Facility facility = auth.getFacility();
      OperationsType type = OperationsType.RF;

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

      if (sender == null || sender.isEmpty()) {
        LOGGER.log(Level.WARNING, "Environment variable 'JAM_EMAIL_SENDER' not found, aborting");
        return;
      }

      final String JLAB_EMAIL_DOMAIN = "@jlab.org";
      String toCsv = "";

      if (watcherList.size() > 0) {
        Watcher watcher = watcherList.get(0);
        String username = watcher.getWatcherPK().getUsername();
        String address = username + JLAB_EMAIL_DOMAIN;
        toCsv += address;
      }

      for (int i = 1; i < watcherList.size(); i++) {
        Watcher watcher = watcherList.get(i);
        String username = watcher.getWatcherPK().getUsername();
        String address = username + JLAB_EMAIL_DOMAIN;
        toCsv += "," + address;
      }

      EmailService emailService = new EmailService();

      emailService.sendEmail(sender, sender, toCsv, null, subject, body, true);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public void sendBeamAuthorizationUpdateEmail(BeamAuthorization auth) {
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

      if (sender == null || sender.isEmpty()) {
        LOGGER.log(Level.WARNING, "Environment variable 'JAM_EMAIL_SENDER' not found, aborting");
        return;
      }

      final String JLAB_EMAIL_DOMAIN = "@jlab.org";
      String toCsv = "";

      if (watcherList.size() > 0) {
        Watcher watcher = watcherList.get(0);
        String username = watcher.getWatcherPK().getUsername();
        String address = username + JLAB_EMAIL_DOMAIN;
        toCsv += address;
      }

      for (int i = 1; i < watcherList.size(); i++) {
        Watcher watcher = watcherList.get(i);
        String username = watcher.getWatcherPK().getUsername();
        String address = username + JLAB_EMAIL_DOMAIN;
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
