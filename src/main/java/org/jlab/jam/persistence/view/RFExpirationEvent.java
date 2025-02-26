package org.jlab.jam.persistence.view;

import java.util.List;
import org.jlab.jam.persistence.entity.*;

public class RFExpirationEvent {
  private Facility facility;
  private List<RFSegmentAuthorization> expiredAuthorizationList;
  private List<RFSegmentAuthorization> upcomingAuthorizationExpirationList;
  private List<RFControlVerification> expiredVerificationList;
  private List<RFControlVerification> upcomingVerificationExpirationList;
  private RFAuthorization authorization;

  public RFExpirationEvent(
      RFAuthorization authorization,
      Facility facility,
      List<RFSegmentAuthorization> expiredAuthorizationList,
      List<RFSegmentAuthorization> upcomingAuthorizationExpirationList,
      List<RFControlVerification> expiredVerificationList,
      List<RFControlVerification> upcomingVerificationExpirationList) {
    this.authorization = authorization;
    this.facility = facility;
    this.expiredAuthorizationList = expiredAuthorizationList;
    this.upcomingAuthorizationExpirationList = upcomingAuthorizationExpirationList;
    this.expiredVerificationList = expiredVerificationList;
    this.upcomingVerificationExpirationList = upcomingVerificationExpirationList;
  }

  public RFAuthorization getAuthorization() {
    return authorization;
  }

  public void setAuthorization(RFAuthorization authorization) {
    this.authorization = authorization;
  }

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public List<RFSegmentAuthorization> getExpiredAuthorizationList() {
    return expiredAuthorizationList;
  }

  public void setExpiredAuthorizationList(List<RFSegmentAuthorization> expiredAuthorizationList) {
    this.expiredAuthorizationList = expiredAuthorizationList;
  }

  public List<RFSegmentAuthorization> getUpcomingAuthorizationExpirationList() {
    return upcomingAuthorizationExpirationList;
  }

  public void setUpcomingAuthorizationExpirationList(
      List<RFSegmentAuthorization> upcomingAuthorizationExpirationList) {
    this.upcomingAuthorizationExpirationList = upcomingAuthorizationExpirationList;
  }

  public List<RFControlVerification> getExpiredVerificationList() {
    return expiredVerificationList;
  }

  public void setExpiredVerificationList(List<RFControlVerification> expiredVerificationList) {
    this.expiredVerificationList = expiredVerificationList;
  }

  public List<RFControlVerification> getUpcomingVerificationExpirationList() {
    return upcomingVerificationExpirationList;
  }

  public void setUpcomingVerificationExpirationList(
      List<RFControlVerification> upcomingVerificationExpirationList) {
    this.upcomingVerificationExpirationList = upcomingVerificationExpirationList;
  }
}
