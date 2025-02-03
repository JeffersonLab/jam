package org.jlab.jam.persistence.entity;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.jlab.jam.persistence.view.BeamDestinationVerification;
import org.jlab.smoothness.persistence.util.YnStringToBoolean;

@Entity
@Table(name = "BEAM_DESTINATION", schema = "JAM_OWNER")
public class BeamDestination {

  @Id
  @Column(name = "BEAM_DESTINATION_ID", nullable = false, precision = 0)
  private BigInteger beamDestinationId;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "FACILITY_ID", referencedColumnName = "FACILITY_ID", nullable = false)
  private Facility facility;

  @Basic
  @Column(name = "CURRENT_LIMIT_UNITS", nullable = false, length = 3)
  private String currentLimitUnits;

  @Basic
  @Column(name = "ACTIVE_YN", nullable = false, length = 1)
  @Convert(converter = YnStringToBoolean.class)
  private boolean active;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "BEAM_DESTINATION_ID")
  private BeamDestinationVerification verification;

  @Size(max = 128)
  @Column(length = 128)
  private String name;

  @OneToMany(mappedBy = "beamDestination", fetch = FetchType.LAZY)
  private List<BeamControlVerification> beamControlVerificationList;

  private BigInteger weight;

  public List<BeamControlVerification> getBeamControlVerificationList() {
    return beamControlVerificationList;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BigInteger getWeight() {
    return weight;
  }

  public void setWeight(BigInteger weight) {
    this.weight = weight;
  }

  public BeamDestinationVerification getVerification() {
    return verification;
  }

  public BigInteger getBeamDestinationId() {
    return beamDestinationId;
  }

  public void setBeamDestinationId(BigInteger beamDestinationId) {
    this.beamDestinationId = beamDestinationId;
  }

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public String getCurrentLimitUnits() {
    return currentLimitUnits;
  }

  public void setCurrentLimitUnits(String currentLimitUnits) {
    this.currentLimitUnits = currentLimitUnits;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BeamDestination that = (BeamDestination) o;
    return Objects.equals(beamDestinationId, that.beamDestinationId)
        && Objects.equals(facility, that.facility)
        && Objects.equals(currentLimitUnits, that.currentLimitUnits)
        && Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(beamDestinationId, facility, currentLimitUnits, name);
  }

  @Override
  public String toString() {
    return "BeamDestination{"
        + "beamDestinationId="
        + beamDestinationId
        + ", facility='"
        + facility
        + '\''
        + ", currentLimitUnits='"
        + currentLimitUnits
        + '\''
        + ", active="
        + active
        + ", verification="
        + verification
        + ", name='"
        + name
        + '\''
        +
        // ", controlVerificationList=" + controlVerificationList +
        ", weight="
        + weight
        + '}';
  }
}
