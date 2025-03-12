package org.jlab.jam.persistence.view;

import java.util.List;
import org.jlab.jam.persistence.entity.*;

public class FacilityUpcomingExpiration {
  private Facility facility;
  private List<BeamDestinationAuthorization> upcomingBeamAuthorizationExpirationList;
  private List<BeamControlVerification> upcomingBeamVerificationExpirationList;
  private List<RFSegmentAuthorization> upcomingRFAuthorizationExpirationList;
  private List<RFControlVerification> upcomingRFVerificationExpirationList;

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public List<BeamDestinationAuthorization> getUpcomingBeamAuthorizationExpirationList() {
    return upcomingBeamAuthorizationExpirationList;
  }

  public void setUpcomingBeamAuthorizationExpirationList(
      List<BeamDestinationAuthorization> upcomingBeamAuthorizationExpirationList) {
    this.upcomingBeamAuthorizationExpirationList = upcomingBeamAuthorizationExpirationList;
  }

  public List<BeamControlVerification> getUpcomingBeamVerificationExpirationList() {
    return upcomingBeamVerificationExpirationList;
  }

  public void setUpcomingBeamVerificationExpirationList(
      List<BeamControlVerification> upcomingBeamVerificationExpirationList) {
    this.upcomingBeamVerificationExpirationList = upcomingBeamVerificationExpirationList;
  }

  public List<RFSegmentAuthorization> getUpcomingRFAuthorizationExpirationList() {
    return upcomingRFAuthorizationExpirationList;
  }

  public void setUpcomingRFAuthorizationExpirationList(
      List<RFSegmentAuthorization> upcomingRFAuthorizationExpirationList) {
    this.upcomingRFAuthorizationExpirationList = upcomingRFAuthorizationExpirationList;
  }

  public List<RFControlVerification> getUpcomingRFVerificationExpirationList() {
    return upcomingRFVerificationExpirationList;
  }

  public void setUpcomingRFVerificationExpirationList(
      List<RFControlVerification> upcomingRFVerificationExpirationList) {
    this.upcomingRFVerificationExpirationList = upcomingRFVerificationExpirationList;
  }
}
