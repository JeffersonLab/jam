package org.jlab.jam.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author ryans
 */
@Entity
@Table(name = "FACILITY", schema = "JAM_OWNER")
public class Facility implements Serializable, Comparable<Facility> {
  private static final long serialVersionUID = 1L;

  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "FACILITY_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger facilityId;

  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 64)
  @Column(nullable = false, length = 64)
  private String name;

  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 32)
  @Column(nullable = false, length = 32)
  private String path;

  @NotNull
  @JoinColumn(name = "RF_WORKGROUP_ID", referencedColumnName = "WORKGROUP_ID", nullable = false)
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  private Workgroup rfWorkgroup;

  @NotNull
  @JoinColumn(name = "BEAM_WORKGROUP_ID", referencedColumnName = "WORKGROUP_ID", nullable = false)
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  private Workgroup beamWorkgroup;

  @Basic(optional = false)
  @NotNull
  @Column(name = "WEIGHT", nullable = false)
  private BigInteger weight;

  public BigInteger getFacilityId() {
    return facilityId;
  }

  public void setFacilityId(BigInteger facilityId) {
    this.facilityId = facilityId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Workgroup getRfWorkgroup() {
    return rfWorkgroup;
  }

  public void setRfWorkgroup(Workgroup rfWorkgroup) {
    this.rfWorkgroup = rfWorkgroup;
  }

  public Workgroup getBeamWorkgroup() {
    return beamWorkgroup;
  }

  public void setBeamWorkgroup(Workgroup beamWorkgroup) {
    this.beamWorkgroup = beamWorkgroup;
  }

  public BigInteger getWeight() {
    return weight;
  }

  public void setWeight(BigInteger weight) {
    this.weight = weight;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 97 * hash + (this.facilityId != null ? this.facilityId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Facility other = (Facility) obj;
    return Objects.equals(this.facilityId, other.facilityId);
  }

  @Override
  public int compareTo(Facility o) {
    return (this.getWeight() == null ? BigInteger.ZERO : this.getWeight())
        .compareTo(o.getWeight() == null ? BigInteger.ZERO : o.getWeight());
  }
}
