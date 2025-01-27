package org.jlab.jam.persistence.entity;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.jlab.smoothness.persistence.util.YnStringToBoolean;

@Entity
@Table(name = "RF_SEGMENT", schema = "JAM_OWNER")
public class RFSegment {

  @Id
  @Column(name = "RF_SEGMENT_ID", nullable = false, precision = 0)
  private BigInteger rfSegmentId;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "FACILITY_ID", referencedColumnName = "FACILITY_ID", nullable = false)
  private Facility facility;

  @Basic
  @Column(name = "ACTIVE_YN", nullable = false, length = 1)
  @Convert(converter = YnStringToBoolean.class)
  private boolean active;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "RF_SEGMENT_ID")
  private RFControlVerification verification;

  @Size(max = 128)
  @Column(length = 128)
  private String name;

  @OneToMany(mappedBy = "rfSegment", fetch = FetchType.LAZY)
  private List<RFControlVerification> rfControlVerificationList;

  private BigInteger weight;

  public List<RFControlVerification> getRFControlVerificationList() {
    return rfControlVerificationList;
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

  public RFControlVerification getVerification() {
    return verification;
  }

  public BigInteger getRFSegmentId() {
    return rfSegmentId;
  }

  public void setRFSegmentId(BigInteger rfSegmentId) {
    this.rfSegmentId = rfSegmentId;
  }

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
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
    RFSegment that = (RFSegment) o;
    return Objects.equals(rfSegmentId, that.rfSegmentId)
        && Objects.equals(facility, that.facility)
        && Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rfSegmentId, facility, name);
  }

  @Override
  public String toString() {
    return "BeamDestination{"
        + "rfSegmentId="
        + rfSegmentId
        + ", facility='"
        + facility
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
