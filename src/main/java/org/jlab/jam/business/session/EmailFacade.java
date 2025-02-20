package org.jlab.jam.business.session;

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
import org.jlab.jam.persistence.view.BeamExpirationEvent;
import org.jlab.jam.persistence.view.RFExpirationEvent;
import org.jlab.jam.persistence.view.TeamExpirationEvent;
import org.jlab.smoothness.business.service.EmailService;
import org.jlab.smoothness.business.service.UserAuthorizationService;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.persistence.view.User;

/**
 * @author ryans
 */
@Stateless
public class EmailFacade extends AbstractFacade<VerificationTeam> {

  private static final Logger LOGGER = Logger.getLogger(EmailFacade.class.getName());

  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @EJB WatcherFacade watcherFacade;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public EmailFacade() {
    super(VerificationTeam.class);
  }

  public static final String EMAIL_DOMAIN = "@jlab.org";

  @PermitAll
  @Asynchronous
  public void sendAsyncExpirationEmails(
      Facility facility, RFExpirationEvent rfEvent, BeamExpirationEvent beamEvent) {
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
}
