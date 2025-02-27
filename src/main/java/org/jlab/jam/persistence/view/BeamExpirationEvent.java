package org.jlab.jam.persistence.view;

import java.util.List;
import org.jlab.jam.persistence.entity.*;

public class BeamExpirationEvent {
  private Facility facility;
  private List<BeamDestinationAuthorization> expiredAuthorizationList;
  private List<BeamDestinationAuthorization> upcomingAuthorizationExpirationList;
  private List<BeamControlVerification> expiredVerificationList;
  private List<BeamControlVerification> upcomingVerificationExpirationList;
  private BeamAuthorization authorization;

  public BeamExpirationEvent(
      BeamAuthorization authorization,
      Facility facility,
      List<BeamDestinationAuthorization> expiredAuthorizationList,
      List<BeamDestinationAuthorization> upcomingAuthorizationExpirationList,
      List<BeamControlVerification> expiredVerificationList,
      List<BeamControlVerification> upcomingVerificationExpirationList) {
    this.authorization = authorization;
    this.facility = facility;
    this.expiredAuthorizationList = expiredAuthorizationList;
    this.upcomingAuthorizationExpirationList = upcomingAuthorizationExpirationList;
    this.expiredVerificationList = expiredVerificationList;
    this.upcomingVerificationExpirationList = upcomingVerificationExpirationList;
  }

  public BeamAuthorization getAuthorization() {
    return authorization;
  }

  public void setAuthorization(BeamAuthorization authorization) {
    this.authorization = authorization;
  }

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public List<BeamDestinationAuthorization> getExpiredAuthorizationList() {
    return expiredAuthorizationList;
  }

  public void setExpiredAuthorizationList(
      List<BeamDestinationAuthorization> expiredAuthorizationList) {
    this.expiredAuthorizationList = expiredAuthorizationList;
  }

  public List<BeamDestinationAuthorization> getUpcomingAuthorizationExpirationList() {
    return upcomingAuthorizationExpirationList;
  }

  public void setUpcomingAuthorizationExpirationList(
      List<BeamDestinationAuthorization> upcomingAuthorizationExpirationList) {
    this.upcomingAuthorizationExpirationList = upcomingAuthorizationExpirationList;
  }

  public List<BeamControlVerification> getExpiredVerificationList() {
    return expiredVerificationList;
  }

  public void setExpiredVerificationList(List<BeamControlVerification> expiredVerificationList) {
    this.expiredVerificationList = expiredVerificationList;
  }

  public List<BeamControlVerification> getUpcomingVerificationExpirationList() {
    return upcomingVerificationExpirationList;
  }

  public void setUpcomingVerificationExpirationList(
      List<BeamControlVerification> upcomingVerificationExpirationList) {
    this.upcomingVerificationExpirationList = upcomingVerificationExpirationList;
  }

  public int getExpirationCount() {
    int count = 0;

    if (expiredAuthorizationList != null) {
      count += expiredAuthorizationList.size();
    }

    if (expiredVerificationList != null) {
      count += expiredVerificationList.size();
    }

    return count;
  }
}
