package org.jlab.jam.business.session;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import org.jlab.jam.persistence.entity.Facility;
import org.jlab.jam.persistence.view.FacilityExpirationEvent;
import org.jlab.jam.persistence.view.FacilityUpcomingExpiration;

@Singleton
@Startup
public class DailyScheduledCheck {

  private static final Logger LOGGER = Logger.getLogger(DailyScheduledCheck.class.getName());

  private Timer timer;
  @Resource private TimerService timerService;
  @EJB ExpirationManager expirationManager;
  @EJB NotificationManager notificationManager;

  @PostConstruct
  private void init() {
    clearTimer();
    startTimer();
  }

  private void clearTimer() {
    LOGGER.log(Level.FINEST, "Clearing Daily Timer");
    for (Timer t : timerService.getTimers()) {
      LOGGER.log(Level.INFO, "Found timer: " + t);
      if ("JAMDailyTimer".equals(t.getInfo())) {
        t.cancel();
      }
    }
    timer = null;
  }

  private void startTimer() {
    LOGGER.log(Level.INFO, "Starting Daily Timer");
    ScheduleExpression schedExp = new ScheduleExpression();
    schedExp.second("0");
    schedExp.minute("0");
    schedExp.hour("0");

    TimerConfig config =
        new TimerConfig(
            "JAMDailyTimer",
            false); // redeploy --keepstate=true might be messing up persistent timers?
    timer = timerService.createCalendarTimer(schedExp, config);
  }

  @Timeout
  private void handleTimeout(Timer timer) {
    LOGGER.log(
        Level.INFO,
        "handleTimeout: Checking for expired / upcoming expiration of authorization and verification...");
    doExpirationCheckAll();
  }

  private void doExpirationCheckAll() {
    Map<Facility, FacilityExpirationEvent> expiredMap = null;
    Map<Facility, FacilityUpcomingExpiration> upcomingMap = null;
    try {
      expiredMap = expirationManager.expireByFacilityAll();

      upcomingMap = expirationManager.getUpcomingExpirationMap(true);

      notificationManager.asyncNotifyExpirationAndUpcoming(expiredMap, upcomingMap);
    } catch (InterruptedException e) {
      LOGGER.log(Level.SEVERE, "handleTimeout: Interrupted", e);
    }
  }

  @RolesAllowed("jam-admin")
  public void doExpirationNow() {
    doExpirationCheckAll();
  }
}
