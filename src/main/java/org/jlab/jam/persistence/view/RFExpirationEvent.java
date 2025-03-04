package org.jlab.jam.persistence.view;

import java.util.List;
import org.jlab.jam.persistence.entity.*;

public class RFExpirationEvent {
  private Facility facility;
  private List<RFSegmentAuthorization> expiredAuthorizationList;
  private List<RFControlVerification> expiredVerificationList;
  private RFAuthorization authorization;

  public RFExpirationEvent(Facility facility) {
    this(null, facility, null, null);
  }

  public RFExpirationEvent(
      RFAuthorization authorization,
      Facility facility,
      List<RFSegmentAuthorization> expiredAuthorizationList,
      List<RFControlVerification> expiredVerificationList) {
    this.authorization = authorization;
    this.facility = facility;
    this.expiredAuthorizationList = expiredAuthorizationList;
    this.expiredVerificationList = expiredVerificationList;
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

  public List<RFControlVerification> getExpiredVerificationList() {
    return expiredVerificationList;
  }

  public void setExpiredVerificationList(List<RFControlVerification> expiredVerificationList) {
    this.expiredVerificationList = expiredVerificationList;
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
