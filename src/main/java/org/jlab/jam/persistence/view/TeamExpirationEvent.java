package org.jlab.jam.persistence.view;

import java.util.ArrayList;
import java.util.List;
import org.jlab.jam.persistence.entity.BeamControlVerification;
import org.jlab.jam.persistence.entity.RFControlVerification;
import org.jlab.jam.persistence.entity.VerificationTeam;

public class TeamExpirationEvent {
  private VerificationTeam team;
  private List<RFControlVerification> rfExpiredVerificationList =
      new ArrayList<RFControlVerification>();
  private List<RFControlVerification> rfUpcomingVerificationExpirationList = new ArrayList<>();
  private List<BeamControlVerification> beamExpiredVerificationList = new ArrayList<>();
  private List<BeamControlVerification> beamUpcomingVerificationExpirationList = new ArrayList<>();

  public TeamExpirationEvent(VerificationTeam team) {
    this.team = team;
  }

  public VerificationTeam getTeam() {
    return team;
  }

  public void setTeam(VerificationTeam team) {
    this.team = team;
  }

  public List<RFControlVerification> getRfExpiredVerificationList() {
    return rfExpiredVerificationList;
  }

  public void setRfExpiredVerificationList(List<RFControlVerification> rfExpiredVerificationList) {
    this.rfExpiredVerificationList = rfExpiredVerificationList;
  }

  public List<RFControlVerification> getRfUpcomingVerificationExpirationList() {
    return rfUpcomingVerificationExpirationList;
  }

  public void setRfUpcomingVerificationExpirationList(
      List<RFControlVerification> rfUpcomingVerificationExpirationList) {
    this.rfUpcomingVerificationExpirationList = rfUpcomingVerificationExpirationList;
  }

  public List<BeamControlVerification> getBeamExpiredVerificationList() {
    return beamExpiredVerificationList;
  }

  public void setBeamExpiredVerificationList(
      List<BeamControlVerification> beamExpiredVerificationList) {
    this.beamExpiredVerificationList = beamExpiredVerificationList;
  }

  public List<BeamControlVerification> getBeamUpcomingVerificationExpirationList() {
    return beamUpcomingVerificationExpirationList;
  }

  public void setBeamUpcomingVerificationExpirationList(
      List<BeamControlVerification> beamUpcomingVerificationExpirationList) {
    this.beamUpcomingVerificationExpirationList = beamUpcomingVerificationExpirationList;
  }
}
