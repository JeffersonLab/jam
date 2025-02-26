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
import org.jlab.jam.persistence.entity.Facility;
import org.jlab.jam.persistence.view.BeamExpirationEvent;
import org.jlab.jam.persistence.view.FacilityExpirationEvent;
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

  public Map<Facility, FacilityExpirationEvent> expireByFacilityAll() throws InterruptedException {
    List<Facility> facilityList = facilityFacade.findAll();
    Map<Facility, FacilityExpirationEvent> eventMap = new HashMap<>();
    for (Facility facility : facilityList) {
      FacilityExpirationEvent event = expireByFacility(facility);
      // checkForUpcoming(event);
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
    RFExpirationEvent rfEvent = rfVerificationFacade.performExpirationCheck(facility, false);
    BeamExpirationEvent beamEvent = beamVerificationFacade.performExpirationCheck(facility, false);

    return new FacilityExpirationEvent(facility, rfEvent, beamEvent);
  }
}
