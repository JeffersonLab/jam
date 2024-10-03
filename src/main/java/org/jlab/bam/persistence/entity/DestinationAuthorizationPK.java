package org.jlab.bam.persistence.entity;

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
  @Column(name = "AUTHORIZATION_ID", nullable = false)
  private BigInteger authorizationId;

  public DestinationAuthorizationPK() {}

  public DestinationAuthorizationPK(BigInteger beamDestinationId, BigInteger authorizationId) {
    this.beamDestinationId = beamDestinationId;
    this.authorizationId = authorizationId;
  }

  public BigInteger getBeamDestinationId() {
    return beamDestinationId;
  }

  public void setBeamDestinationId(BigInteger beamDestinationId) {
    this.beamDestinationId = beamDestinationId;
  }

  public BigInteger getAuthorizationId() {
    return authorizationId;
  }

  public void setAuthorizationId(BigInteger authorizationId) {
    this.authorizationId = authorizationId;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (beamDestinationId != null ? beamDestinationId.hashCode() : 0);
    hash += (authorizationId != null ? authorizationId.hashCode() : 0);
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
    return (this.authorizationId != null || other.authorizationId == null)
        && (this.authorizationId == null || this.authorizationId.equals(other.authorizationId));
  }

  @Override
  public String toString() {
    return "org.jlab.beamauth.persistence.entity.DestinationAuthorizationPK[ beamDestinationId="
        + beamDestinationId
        + ", authorizationId="
        + authorizationId
        + " ]";
  }
}
