package org.jlab.jam.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author ryans
 */
@Entity
@Table(name = "RF_SEGMENT_AUTHORIZATION", schema = "JAM_OWNER")
@NamedQueries({
  @NamedQuery(
      name = "RFSegmentAuthorization.findAll",
      query = "SELECT d FROM RFSegmentAuthorization d")
})
public class RFSegmentAuthorization implements Serializable {

  private static final long serialVersionUID = 1L;
  @EmbeddedId protected SegmentAuthorizationPK segmentAuthorizationPK;

  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 16)
  @Column(name = "RF_MODE", nullable = false, length = 16)
  private String rfMode;

  @Basic(optional = true)
  @Size(max = 256)
  @Column(name = "COMMENTS", nullable = true, length = 256)
  private String comments;

  @Column(name = "EXPIRATION_DATE")
  @Temporal(TemporalType.TIMESTAMP)
  private Date expirationDate;

  @JoinColumn(
      name = "RF_SEGMENT_ID",
      referencedColumnName = "RF_SEGMENT_ID",
      nullable = false,
      insertable = false,
      updatable = false)
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private RFSegment segment;

  @JoinColumn(
      name = "RF_AUTHORIZATION_ID",
      referencedColumnName = "RF_AUTHORIZATION_ID",
      nullable = false,
      insertable = false,
      updatable = false)
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private RFAuthorization rfAuthorization;

  public RFSegmentAuthorization() {}

  public RFSegmentAuthorization(SegmentAuthorizationPK segmentAuthorizationPK) {
    this.segmentAuthorizationPK = segmentAuthorizationPK;
  }

  public RFSegmentAuthorization(BigInteger rfSegmentId, BigInteger rfAuthorizationId) {
    this.segmentAuthorizationPK = new SegmentAuthorizationPK(rfSegmentId, rfAuthorizationId);
  }

  public SegmentAuthorizationPK getSegmentAuthorizationPK() {
    return segmentAuthorizationPK;
  }

  public void setSegmentAuthorizationPK(SegmentAuthorizationPK segmentAuthorizationPK) {
    this.segmentAuthorizationPK = segmentAuthorizationPK;
  }

  public RFSegment getSegment() {
    return segment;
  }

  public String getRFMode() {
    return rfMode;
  }

  public void setRFMode(String rfMode) {
    this.rfMode = rfMode;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  public RFAuthorization getRFAuthorization() {
    return rfAuthorization;
  }

  public void setRFAuthorization(RFAuthorization rfAuthorization) {
    this.rfAuthorization = rfAuthorization;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (segmentAuthorizationPK != null ? segmentAuthorizationPK.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof RFSegmentAuthorization)) {
      return false;
    }
    RFSegmentAuthorization other = (RFSegmentAuthorization) object;
    return (this.segmentAuthorizationPK != null || other.segmentAuthorizationPK == null)
        && (this.segmentAuthorizationPK == null
            || this.segmentAuthorizationPK.equals(other.segmentAuthorizationPK));
  }

  @Override
  public String toString() {
    return "org.jlab.jam.persistence.entity.RFSegmentAuthorization[segmentAuthorizationPK="
        + segmentAuthorizationPK
        + " ]";
  }

  public RFSegmentAuthorization createAdminClone(RFAuthorization authClone) {
    RFSegmentAuthorization other = new RFSegmentAuthorization();
    other.rfAuthorization = authClone;
    other.comments = this.comments;
    other.expirationDate = this.expirationDate;
    other.segment = this.segment;

    return other;
  }
}
