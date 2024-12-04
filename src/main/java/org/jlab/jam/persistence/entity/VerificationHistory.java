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
@Table(name = "VERIFICATION_HISTORY", schema = "JAM_OWNER")
public class VerificationHistory implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(
      name = "VerificationHistoryId",
      sequenceName = "VERIFICATION_HISTORY_ID",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VerificationHistoryId")
  @Basic(optional = false)
  @NotNull
  @Column(name = "VERIFICATION_HISTORY_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger verificationHistoryId;

  @Column(name = "VERIFICATION_ID")
  @NotNull
  private Integer verificationId;

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

  @JoinColumn(name = "CONTROL_VERIFICATION_ID", referencedColumnName = "CONTROL_VERIFICATION_ID")
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private ControlVerification controlVerification;

  public VerificationHistory() {}

  public BigInteger getVerificationHistoryId() {
    return verificationHistoryId;
  }

  public void setVerificationHistoryId(BigInteger verificationHistoryId) {
    this.verificationHistoryId = verificationHistoryId;
  }

  public Integer getVerificationId() {
    return verificationId;
  }

  public void setVerificationId(Integer verificationId) {
    this.verificationId = verificationId;
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

  public ControlVerification getControlVerification() {
    return controlVerification;
  }

  public void setControlVerification(ControlVerification controlVerificationId) {
    this.controlVerification = controlVerificationId;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (verificationHistoryId != null ? verificationHistoryId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof VerificationHistory)) {
      return false;
    }
    VerificationHistory other = (VerificationHistory) object;
    return (this.verificationHistoryId != null || other.verificationHistoryId == null)
        && (this.verificationHistoryId == null
            || this.verificationHistoryId.equals(other.verificationHistoryId));
  }

  @Override
  public String toString() {
    return "org.jlab.beamauth.persistence.entity.VerificationHistory[ verificationHistoryId="
        + verificationHistoryId
        + " ]";
  }
}
