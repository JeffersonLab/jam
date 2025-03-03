package org.jlab.jam.persistence.view;

import java.util.ArrayList;
import java.util.List;
import org.jlab.jam.persistence.entity.BeamControlVerification;
import org.jlab.jam.persistence.entity.RFControlVerification;
import org.jlab.jam.persistence.entity.VerificationTeam;

public class TeamUpcoming {
  private VerificationTeam team;
  private List<RFControlVerification> rfUpcomingVerificationExpirationList = new ArrayList<>();
  private List<BeamControlVerification> beamUpcomingVerificationExpirationList = new ArrayList<>();

  public TeamUpcoming(VerificationTeam team) {
    this.team = team;
  }

  public VerificationTeam getTeam() {
    return team;
  }

  public void setTeam(VerificationTeam team) {
    this.team = team;
  }

  public List<RFControlVerification> getRfUpcomingVerificationExpirationList() {
    return rfUpcomingVerificationExpirationList;
  }

  public void setRfUpcomingVerificationExpirationList(
      List<RFControlVerification> rfUpcomingVerificationExpirationList) {
    this.rfUpcomingVerificationExpirationList = rfUpcomingVerificationExpirationList;
  }

  public List<BeamControlVerification> getBeamUpcomingVerificationExpirationList() {
    return beamUpcomingVerificationExpirationList;
  }

  public void setBeamUpcomingVerificationExpirationList(
      List<BeamControlVerification> beamUpcomingVerificationExpirationList) {
    this.beamUpcomingVerificationExpirationList = beamUpcomingVerificationExpirationList;
  }
}
