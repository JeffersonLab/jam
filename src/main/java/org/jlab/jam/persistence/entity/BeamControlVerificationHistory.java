package org.jlab.jam.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 * @author ryans
 */
@Entity
@Table(name = "BEAM_CONTROL_VERIFICATION_HISTORY", schema = "JAM_OWNER")
public class BeamControlVerificationHistory implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(
      name = "BeamControlVerificationHistoryId",
      sequenceName = "BEAM_CONTROL_VERIFICATION_HISTORY_ID",
      allocationSize = 1)
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "BeamControlVerificationHistoryId")
  @Basic(optional = false)
  @NotNull
  @Column(
      name = "BEAM_CONTROL_VERIFICATION_HISTORY_ID",
      nullable = false,
      precision = 22,
      scale = 0)
  private BigInteger beamControlVerificationHistoryId;

  @Column(name = "VERIFICATION_STATUS_ID")
  @NotNull
  private Integer verificationStatusId;

  @Column(name = "MODIFIED_BY", nullable = false)
  private String modifiedBy;

  @Basic(optional = false)
  @NotNull
  @Column(name = "MODIFIED_DATE", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date modifiedDate;

  @Column(name = "VERIFIED_BY", nullable = true)
  private String verifiedBy;

  @Basic(optional = false)
  @NotNull
  @Column(name = "VERIFICATION_DATE", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date verificationDate;

  @Basic(optional = true)
  @Column(name = "EXPIRATION_DATE", nullable = true)
  @Temporal(TemporalType.TIMESTAMP)
  private Date expirationDate;

  @Basic(optional = true)
  @Column(name = "COMMENTS", nullable = true)
  private String comments;

  @JoinColumn(
      name = "BEAM_CONTROL_VERIFICATION_ID",
      referencedColumnName = "BEAM_CONTROL_VERIFICATION_ID")
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private BeamControlVerification beamControlVerification;

  public BeamControlVerificationHistory() {}

  public BigInteger getBeamControlVerificationHistoryId() {
    return beamControlVerificationHistoryId;
  }

  public void setBeamControlVerificationHistoryId(BigInteger beamControlVerificationHistoryId) {
    this.beamControlVerificationHistoryId = beamControlVerificationHistoryId;
  }

  public Integer getVerificationStatusId() {
    return verificationStatusId;
  }

  public void setVerificationStatusId(Integer verificationStatusId) {
    this.verificationStatusId = verificationStatusId;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public Date getModifiedDate() {
    return modifiedDate;
  }

  public void setModifiedDate(Date modifiedDate) {
    this.modifiedDate = modifiedDate;
  }

  public String getVerifiedBy() {
    return verifiedBy;
  }

  public void setVerifiedBy(String verifiedBy) {
    this.verifiedBy = verifiedBy;
  }

  public Date getVerificationDate() {
    return verificationDate;
  }

  public void setVerificationDate(Date verificationDate) {
    this.verificationDate = verificationDate;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public BeamControlVerification getBeamControlVerification() {
    return beamControlVerification;
  }

  public void setBeamControlVerification(BeamControlVerification beamControlVerificationId) {
    this.beamControlVerification = beamControlVerificationId;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash +=
        (beamControlVerificationHistoryId != null
            ? beamControlVerificationHistoryId.hashCode()
            : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof BeamControlVerificationHistory)) {
      return false;
    }
    BeamControlVerificationHistory other = (BeamControlVerificationHistory) object;
    return (this.beamControlVerificationHistoryId != null
            || other.beamControlVerificationHistoryId == null)
        && (this.beamControlVerificationHistoryId == null
            || this.beamControlVerificationHistoryId.equals(
                other.beamControlVerificationHistoryId));
  }

  @Override
  public String toString() {
    return "org.jlab.jam.persistence.entity.BeamControlVerificationHistory[ beamControlVerificationHistoryId="
        + beamControlVerificationHistoryId
        + " ]";
  }
}
