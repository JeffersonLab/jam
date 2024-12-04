package org.jlab.jam.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author ryans
 */
@Entity
@Table(name = "CONTROL_VERIFICATION", schema = "BAM_OWNER")
@NamedQueries({
  @NamedQuery(name = "ControlVerification.findAll", query = "SELECT c FROM ControlVerification c")
})
public class ControlVerification implements Serializable, Comparable<ControlVerification> {
  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(
      name = "ControlVerificationId",
      sequenceName = "CONTROL_VERIFICATION_ID",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ControlVerificationId")
  @Basic(optional = false)
  @NotNull
  @Column(name = "CONTROL_VERIFICATION_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger controlVerificationId;

  @NotNull
  @JoinColumn(name = "BEAM_DESTINATION_ID", referencedColumnName = "BEAM_DESTINATION_ID")
  @ManyToOne(fetch = FetchType.EAGER)
  private BeamDestination beamDestination;

  @Basic(optional = false)
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

  @Column(name = "VERIFICATION_DATE")
  @Temporal(TemporalType.TIMESTAMP)
  private Date verificationDate;

  @Column(name = "VERIFIED_BY", nullable = true)
  private String verifiedBy;

  @Column(name = "EXPIRATION_DATE")
  @Temporal(TemporalType.TIMESTAMP)
  private Date expirationDate;

  @Size(max = 2048)
  @Column(length = 2048)
  private String comments;

  @OneToMany(mappedBy = "controlVerification", fetch = FetchType.LAZY)
  private List<VerificationHistory> verificationHistoryList;

  @JoinColumn(name = "CREDITED_CONTROL_ID", referencedColumnName = "CREDITED_CONTROL_ID")
  @ManyToOne(fetch = FetchType.LAZY)
  private CreditedControl creditedControl;

  public ControlVerification() {}

  public BigInteger getControlVerificationId() {
    return controlVerificationId;
  }

  public void setControlVerificationId(BigInteger controlVerificationId) {
    this.controlVerificationId = controlVerificationId;
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

  public BeamDestination getBeamDestination() {
    return beamDestination;
  }

  public void setBeamDestination(BeamDestination beamDestination) {
    this.beamDestination = beamDestination;
  }

  public Integer getVerificationId() {
    return verificationId;
  }

  public void setVerificationId(Integer verificationId) {
    this.verificationId = verificationId;
  }

  public Date getVerificationDate() {
    return verificationDate;
  }

  public void setVerificationDate(Date verificationDate) {
    this.verificationDate = verificationDate;
  }

  public String getVerifiedBy() {
    return verifiedBy;
  }

  public void setVerifiedBy(String verifiedBy) {
    this.verifiedBy = verifiedBy;
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

  public List<VerificationHistory> getVerificationHistoryList() {
    return verificationHistoryList;
  }

  public void setVerificationHistoryList(List<VerificationHistory> verificationHistoryList) {
    this.verificationHistoryList = verificationHistoryList;
  }

  public CreditedControl getCreditedControl() {
    return creditedControl;
  }

  public void setCreditedControl(CreditedControl creditedControl) {
    this.creditedControl = creditedControl;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (controlVerificationId != null ? controlVerificationId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof ControlVerification)) {
      return false;
    }
    ControlVerification other = (ControlVerification) object;
    return (this.controlVerificationId != null || other.controlVerificationId == null)
        && (this.controlVerificationId == null
            || this.controlVerificationId.equals(other.controlVerificationId));
  }

  @Override
  public String toString() {
    return "org.jlab.beamauth.persistence.entity.ControlVerification[ controlVerificationId="
        + controlVerificationId
        + " ]";
  }

  @Override
  public int compareTo(ControlVerification o) {
    return this.beamDestination.getWeight().compareTo(o.beamDestination.getWeight());
  }
}
