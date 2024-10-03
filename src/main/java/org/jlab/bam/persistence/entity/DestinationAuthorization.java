package org.jlab.bam.persistence.entity;

import java.io.Serializable;
import java.math.BigDecimal;
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
@Table(name = "DESTINATION_AUTHORIZATION", schema = "BAM_OWNER")
@NamedQueries({
  @NamedQuery(
      name = "DestinationAuthorization.findAll",
      query = "SELECT d FROM DestinationAuthorization d")
})
public class DestinationAuthorization implements Serializable {

  private static final long serialVersionUID = 1L;
  @EmbeddedId protected DestinationAuthorizationPK destinationAuthorizationPK;

  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 16)
  @Column(name = "BEAM_MODE", nullable = false, length = 16)
  private String beamMode;

  @Column(name = "CW_LIMIT", precision = 24, scale = 12)
  private BigDecimal cwLimit;

  @Basic(optional = true)
  @Size(max = 256)
  @Column(name = "COMMENTS", nullable = true, length = 256)
  private String comments;

  @Column(name = "EXPIRATION_DATE")
  @Temporal(TemporalType.TIMESTAMP)
  private Date expirationDate;

  @JoinColumn(
      name = "BEAM_DESTINATION_ID",
      referencedColumnName = "BEAM_DESTINATION_ID",
      nullable = false,
      insertable = false,
      updatable = false)
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private BeamDestination destination;

  @JoinColumn(
      name = "AUTHORIZATION_ID",
      referencedColumnName = "AUTHORIZATION_ID",
      nullable = false,
      insertable = false,
      updatable = false)
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Authorization authorization;

  public DestinationAuthorization() {}

  public DestinationAuthorization(DestinationAuthorizationPK destinationAuthorizationPK) {
    this.destinationAuthorizationPK = destinationAuthorizationPK;
  }

  public DestinationAuthorization(
      DestinationAuthorizationPK destinationAuthorizationPK, String beamMode) {
    this.destinationAuthorizationPK = destinationAuthorizationPK;
    this.beamMode = beamMode;
  }

  public DestinationAuthorization(BigInteger beamDestinationId, BigInteger authorizationId) {
    this.destinationAuthorizationPK =
        new DestinationAuthorizationPK(beamDestinationId, authorizationId);
  }

  public DestinationAuthorizationPK getDestinationAuthorizationPK() {
    return destinationAuthorizationPK;
  }

  public void setDestinationAuthorizationPK(DestinationAuthorizationPK destinationAuthorizationPK) {
    this.destinationAuthorizationPK = destinationAuthorizationPK;
  }

  public BeamDestination getDestination() {
    return destination;
  }

  public String getBeamMode() {
    return beamMode;
  }

  public void setBeamMode(String beamMode) {
    this.beamMode = beamMode;
  }

  public BigDecimal getCwLimit() {
    return cwLimit;
  }

  public void setCwLimit(BigDecimal cwLimit) {
    this.cwLimit = cwLimit;
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

  public Authorization getAuthorization() {
    return authorization;
  }

  public void setAuthorization(Authorization authorization) {
    this.authorization = authorization;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (destinationAuthorizationPK != null ? destinationAuthorizationPK.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof DestinationAuthorization)) {
      return false;
    }
    DestinationAuthorization other = (DestinationAuthorization) object;
    return (this.destinationAuthorizationPK != null || other.destinationAuthorizationPK == null)
        && (this.destinationAuthorizationPK == null
            || this.destinationAuthorizationPK.equals(other.destinationAuthorizationPK));
  }

  @Override
  public String toString() {
    return "org.jlab.beamauth.persistence.entity.DestinationAuthorization[ destinationAuthorizationPK="
        + destinationAuthorizationPK
        + " ]";
  }

  public DestinationAuthorization createAdminClone(Authorization authClone) {
    DestinationAuthorization other = new DestinationAuthorization();
    other.authorization = authClone;
    other.beamMode = this.beamMode;
    other.comments = this.comments;
    other.cwLimit = this.cwLimit;
    other.expirationDate = this.expirationDate;
    other.destination = this.destination;

    return other;
  }
}
