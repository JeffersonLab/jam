package org.jlab.jam.business.session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import org.jlab.jam.persistence.entity.*;
import org.jlab.jam.persistence.view.BeamExpirationEvent;
import org.jlab.jam.persistence.view.FacilityExpirationEvent;
import org.jlab.jam.persistence.view.FacilityUpcomingExpiration;
import org.jlab.jam.persistence.view.RFExpirationEvent;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ExpirationManager {
  private final ConcurrentHashMap<Facility, ReentrantLock> map = new ConcurrentHashMap<>();

  private final long timeout = 10;
  private final TimeUnit unit = TimeUnit.SECONDS;

  @EJB RFControlVerificationFacade rfVerificationFacade;
  @EJB BeamControlVerificationFacade beamVerificationFacade;
  @EJB FacilityFacade facilityFacade;
  @EJB RFAuthorizationFacade rfAuthorizationFacade;
  @EJB BeamAuthorizationFacade beamAuthorizationFacade;

  // Anything within 7 days of expiration is considered upcoming, but
  // Only things between day 6 and 7 are considered in the notification boundary
  // As to avoid spamming users daily (there is a once a day scheduled notification).
  public Map<Facility, FacilityUpcomingExpiration> getUpcomingExpirationMap(boolean boundary) {
    List<Facility> facilityList = facilityFacade.findAll();
    Map<Facility, FacilityUpcomingExpiration> map = new HashMap<>();

    for (Facility facility : facilityList) {
      FacilityUpcomingExpiration upcoming = upcomingByFacility(facility, boundary);

      map.put(facility, upcoming);
    }

    return map;
  }

  private FacilityUpcomingExpiration upcomingByFacility(Facility facility, boolean boundary) {
    FacilityUpcomingExpiration upcoming = new FacilityUpcomingExpiration();

    List<RFControlVerification> rfVerificationList =
        rfVerificationFacade.checkForUpcomingVerificationExpirations(facility, boundary);
    List<BeamControlVerification> beamVerificationList =
        beamVerificationFacade.checkForUpcomingVerificationExpirations(facility, boundary);

    RFAuthorization rfAuth = rfAuthorizationFacade.findCurrent(facility);
    List<RFSegmentAuthorization> upcomingRFAuthorizationExpirationList = null;
    if (rfAuth != null) {
      upcomingRFAuthorizationExpirationList =
          rfVerificationFacade.checkForUpcomingAuthorizationExpirations(rfAuth, boundary);
    }

    BeamAuthorization beamAuth = beamAuthorizationFacade.findCurrent(facility);
    List<BeamDestinationAuthorization> upcomingBeamAuthorizationExpirationList = null;
    if (beamAuth != null) {
      upcomingBeamAuthorizationExpirationList =
          beamVerificationFacade.checkForUpcomingAuthorizationExpirations(beamAuth, boundary);
    }

    upcoming.setFacility(facility);
    upcoming.setUpcomingBeamVerificationExpirationList(beamVerificationList);
    upcoming.setUpcomingRFVerificationExpirationList(rfVerificationList);
    upcoming.setUpcomingRFAuthorizationExpirationList(upcomingRFAuthorizationExpirationList);
    upcoming.setUpcomingBeamAuthorizationExpirationList(upcomingBeamAuthorizationExpirationList);

    return upcoming;
  }

  public Map<Facility, FacilityExpirationEvent> expireByFacilityAll() throws InterruptedException {
    List<Facility> facilityList = facilityFacade.findAll();
    Map<Facility, FacilityExpirationEvent> eventMap = new HashMap<>();
    for (Facility facility : facilityList) {
      FacilityExpirationEvent event = expireByFacility(facility);
      eventMap.put(facility, event);
    }

    return eventMap;
  }

  public FacilityExpirationEvent expireByFacility(Facility facility) throws InterruptedException {
    FacilityExpirationEvent event;

    ReentrantLock lock = map.computeIfAbsent(facility, k -> new ReentrantLock());

    lock.tryLock(timeout, unit);
    try {
      event = expireByFacilitySingleThreaded(facility);
    } finally {
      lock.unlock();
    }

    return event;
  }

  private FacilityExpirationEvent expireByFacilitySingleThreaded(Facility facility) {
    System.err.println("Facility Expiration check: " + facility.getName());
    RFExpirationEvent rfEvent = rfVerificationFacade.performExpirationCheck(facility);
    BeamExpirationEvent beamEvent = beamVerificationFacade.performExpirationCheck(facility);

    return new FacilityExpirationEvent(facility, rfEvent, beamEvent);
  }
}
