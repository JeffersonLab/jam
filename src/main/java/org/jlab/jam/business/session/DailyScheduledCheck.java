package org.jlab.jam.business.session;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

@Singleton
@Startup
public class DailyScheduledCheck {

  private static final Logger LOGGER = Logger.getLogger(DailyScheduledCheck.class.getName());

  private Timer timer;
  @Resource private TimerService timerService;
  @EJB RFControlVerificationFacade rfVerificationFacade;
  @EJB BeamControlVerificationFacade beamVerificationFacade;

  @PostConstruct
  private void init() {
    clearTimer();
    startTimer();
  }

  private void clearTimer() {
    LOGGER.log(Level.FINEST, "Clearing Daily Timer");
    for (Timer t : timerService.getTimers()) {
      LOGGER.log(Level.INFO, "Found timer: " + t);
      if ("BAMDailyTimer".equals(t.getInfo())) {
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
            "BAMDailyTimer",
            false); // redeploy --keepstate=true might be messing up persistent timers?
    timer = timerService.createCalendarTimer(schedExp, config);
  }

  @Timeout
  private void handleTimeout(Timer timer) {
    LOGGER.log(
        Level.INFO,
        "handleTimeout: Checking for expired / upcoming expiration of authorization and verification...");
    rfVerificationFacade.performExpirationCheckAll();
    beamVerificationFacade.performExpirationCheckAll();
  }
}
