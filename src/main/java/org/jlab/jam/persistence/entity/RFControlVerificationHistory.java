package org.jlab.jam.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author ryans
 */
@Entity
@Table(name = "RF_CONTROL_VERIFICATION_HISTORY", schema = "JAM_OWNER")
public class RFControlVerificationHistory implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(
      name = "RFControlVerificationHistoryId",
      sequenceName = "RF_CONTROL_VERIFICATION_HISTORY_ID",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RFControlVerificationHistoryId")
  @Basic(optional = false)
  @NotNull
  @Column(name = "RF_CONTROL_VERIFICATION_HISTORY_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger rfControlVerificationHistoryId;

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

  @Size(max = 4000)
  @Column(name = "EXTERNAL_URL", length = 4000)
  private String externalUrl;

  @JoinColumn(
      name = "RF_CONTROL_VERIFICATION_ID",
      referencedColumnName = "RF_CONTROL_VERIFICATION_ID")
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private RFControlVerification rfControlVerification;

  public RFControlVerificationHistory() {}

  public BigInteger getRFControlVerificationHistoryId() {
    return rfControlVerificationHistoryId;
  }

  public void setRFControlVerificationHistoryId(BigInteger rfControlVerificationHistoryId) {
    this.rfControlVerificationHistoryId = rfControlVerificationHistoryId;
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

  public String getExternalUrl() {
    return externalUrl;
  }

  public void setExternalUrl(String externalUrl) {
    this.externalUrl = externalUrl;
  }

  public RFControlVerification getRFControlVerification() {
    return rfControlVerification;
  }

  public void setRFControlVerification(RFControlVerification rfControlVerificationId) {
    this.rfControlVerification = rfControlVerificationId;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash +=
        (rfControlVerificationHistoryId != null ? rfControlVerificationHistoryId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof RFControlVerificationHistory)) {
      return false;
    }
    RFControlVerificationHistory other = (RFControlVerificationHistory) object;
    return (this.rfControlVerificationHistoryId != null
            || other.rfControlVerificationHistoryId == null)
        && (this.rfControlVerificationHistoryId == null
            || this.rfControlVerificationHistoryId.equals(other.rfControlVerificationHistoryId));
  }

  @Override
  public String toString() {
    return "org.jlab.jam.persistence.entity.RFControlVerificationHistory[ rfControlVerificationHistoryId="
        + rfControlVerificationHistoryId
        + " ]";
  }
}
