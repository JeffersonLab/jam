package org.jlab.jam.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * @author ryans
 */
@Embeddable
public class DestinationAuthorizationPK implements Serializable {
  @Basic(optional = false)
  @NotNull
  @Column(name = "BEAM_DESTINATION_ID", nullable = false)
  private BigInteger beamDestinationId;

  @Basic(optional = false)
  @NotNull
  @Column(name = "BEAM_AUTHORIZATION_ID", nullable = false)
  private BigInteger beamAuthorizationId;

  public DestinationAuthorizationPK() {}

  public DestinationAuthorizationPK(BigInteger beamDestinationId, BigInteger beamAuthorizationId) {
    this.beamDestinationId = beamDestinationId;
    this.beamAuthorizationId = beamAuthorizationId;
  }

  public BigInteger getBeamDestinationId() {
    return beamDestinationId;
  }

  public void setBeamDestinationId(BigInteger beamDestinationId) {
    this.beamDestinationId = beamDestinationId;
  }

  public BigInteger getBeamAuthorizationId() {
    return beamAuthorizationId;
  }

  public void setAuthorizationId(BigInteger beamAuthorizationId) {
    this.beamAuthorizationId = beamAuthorizationId;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (beamDestinationId != null ? beamDestinationId.hashCode() : 0);
    hash += (beamAuthorizationId != null ? beamAuthorizationId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof DestinationAuthorizationPK)) {
      return false;
    }
    DestinationAuthorizationPK other = (DestinationAuthorizationPK) object;
    if ((this.beamDestinationId == null && other.beamDestinationId != null)
        || (this.beamDestinationId != null
            && !this.beamDestinationId.equals(other.beamDestinationId))) {
      return false;
    }
    return (this.beamAuthorizationId != null || other.beamAuthorizationId == null)
        && (this.beamAuthorizationId == null
            || this.beamAuthorizationId.equals(other.beamAuthorizationId));
  }

  @Override
  public String toString() {
    return "org.jlab.jam.persistence.entity.DestinationAuthorizationPK[ beamDestinationId="
        + beamDestinationId
        + ", beamAuthorizationId="
        + beamAuthorizationId
        + " ]";
  }
}
