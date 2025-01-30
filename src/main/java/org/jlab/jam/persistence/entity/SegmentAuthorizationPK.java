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
public class SegmentAuthorizationPK implements Serializable {
  @Basic(optional = false)
  @NotNull
  @Column(name = "RF_SEGMENT_ID", nullable = false)
  private BigInteger rfSegmentId;

  @Basic(optional = false)
  @NotNull
  @Column(name = "RF_AUTHORIZATION_ID", nullable = false)
  private BigInteger rfAuthorizationId;

  public SegmentAuthorizationPK() {}

  public SegmentAuthorizationPK(BigInteger rfSegmentId, BigInteger rfAuthorizationId) {
    this.rfSegmentId = rfSegmentId;
    this.rfAuthorizationId = rfAuthorizationId;
  }

  public BigInteger getRFSegmentId() {
    return rfSegmentId;
  }

  public void setRFSegmentId(BigInteger rfSegmentId) {
    this.rfSegmentId = rfSegmentId;
  }

  public BigInteger getRFAuthorizationId() {
    return rfAuthorizationId;
  }

  public void setRFAuthorizationId(BigInteger rfAuthorizationId) {
    this.rfAuthorizationId = rfAuthorizationId;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (rfSegmentId != null ? rfSegmentId.hashCode() : 0);
    hash += (rfAuthorizationId != null ? rfAuthorizationId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof SegmentAuthorizationPK)) {
      return false;
    }
    SegmentAuthorizationPK other = (SegmentAuthorizationPK) object;
    if ((this.rfSegmentId == null && other.rfSegmentId != null)
        || (this.rfSegmentId != null && !this.rfSegmentId.equals(other.rfSegmentId))) {
      return false;
    }
    return (this.rfAuthorizationId != null || other.rfAuthorizationId == null)
        && (this.rfAuthorizationId == null
            || this.rfAuthorizationId.equals(other.rfAuthorizationId));
  }

  @Override
  public String toString() {
    return "org.jlab.jam.persistence.entity.SegmentAuthorizationPK[ rfSegmentId="
        + rfSegmentId
        + ", authorizationId="
        + rfAuthorizationId
        + " ]";
  }
}
