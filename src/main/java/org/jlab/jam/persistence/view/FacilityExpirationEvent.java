package org.jlab.jam.persistence.view;

import org.jlab.jam.persistence.entity.Facility;

public class FacilityExpirationEvent {
  private Facility facility;
  private RFExpirationEvent rfEvent;
  private BeamExpirationEvent beamEvent;

  public FacilityExpirationEvent(
      Facility facility, RFExpirationEvent rfEvent, BeamExpirationEvent beamEvent) {
    this.facility = facility;
    this.rfEvent = rfEvent;
    this.beamEvent = beamEvent;
  }

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public RFExpirationEvent getRfEvent() {
    return rfEvent;
  }

  public void setRfEvent(RFExpirationEvent rfEvent) {
    this.rfEvent = rfEvent;
  }

  public BeamExpirationEvent getBeamEvent() {
    return beamEvent;
  }

  public void setBeamEvent(BeamExpirationEvent beamEvent) {
    this.beamEvent = beamEvent;
  }

  @Override
  public String toString() {
    return "FacilityExpirationEvent{"
        + "facility="
        + facility.getName()
        + ", rfEvent="
        + rfEvent.getExpirationCount()
        + ", beamEvent="
        + beamEvent.getExpirationCount()
        + '}';
  }

  public int getExpirationCount() {
    return rfEvent.getExpirationCount() + beamEvent.getExpirationCount();
  }
}
