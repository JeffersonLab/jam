package org.jlab.jam.persistence.view;

import java.util.List;
import org.jlab.jam.persistence.entity.*;

public class BeamExpirationEvent {
  private Facility facility;
  private List<BeamDestinationAuthorization> expiredAuthorizationList;
  private List<BeamControlVerification> expiredVerificationList;
  private BeamAuthorization authorization;

  public BeamExpirationEvent(
      BeamAuthorization authorization,
      Facility facility,
      List<BeamDestinationAuthorization> expiredAuthorizationList,
      List<BeamControlVerification> expiredVerificationList) {
    this.authorization = authorization;
    this.facility = facility;
    this.expiredAuthorizationList = expiredAuthorizationList;
    this.expiredVerificationList = expiredVerificationList;
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

  public List<BeamControlVerification> getExpiredVerificationList() {
    return expiredVerificationList;
  }

  public void setExpiredVerificationList(List<BeamControlVerification> expiredVerificationList) {
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
