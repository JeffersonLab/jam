package org.jlab.jam.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author ryans
 */
@Entity
@Table(name = "RF_CONTROL_VERIFICATION", schema = "JAM_OWNER")
@NamedQueries({
  @NamedQuery(
      name = "RFControlVerification.findAll",
      query = "SELECT c FROM RFControlVerification c")
})
public class RFControlVerification implements Serializable, Comparable<RFControlVerification> {
  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(
      name = "RFControlVerificationId",
      sequenceName = "RF_CONTROL_VERIFICATION_ID",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RFControlVerificationId")
  @Basic(optional = false)
  @NotNull
  @Column(name = "RF_CONTROL_VERIFICATION_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger rfControlVerificationId;

  @NotNull
  @JoinColumn(name = "RF_SEGMENT_ID", referencedColumnName = "RF_SEGMENT_ID")
  @ManyToOne(fetch = FetchType.EAGER)
  private RFSegment rfSegment;

  @Basic(optional = false)
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

  @Size(max = 4000)
  @Column(name = "EXTERNAL_URL", length = 4000)
  private String externalUrl;

  @OneToMany(mappedBy = "rfControlVerification", fetch = FetchType.LAZY)
  private List<RFControlVerificationHistory> rfControlVerificationHistoryList;

  @JoinColumn(name = "CREDITED_CONTROL_ID", referencedColumnName = "CREDITED_CONTROL_ID")
  @ManyToOne(fetch = FetchType.LAZY)
  private CreditedControl creditedControl;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "RF_CONTROL_VERIFICATION_COMPONENT",
      joinColumns = @JoinColumn(name = "RF_CONTROL_VERIFICATION_ID"),
      inverseJoinColumns = @JoinColumn(name = "COMPONENT_ID"))
  private List<Component> componentList;

  public RFControlVerification() {}

  public BigInteger getRFControlVerificationId() {
    return rfControlVerificationId;
  }

  public void setRFControlVerificationId(BigInteger rfControlVerificationId) {
    this.rfControlVerificationId = rfControlVerificationId;
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

  public RFSegment getRFSegment() {
    return rfSegment;
  }

  public void setRFSegment(RFSegment rfSegment) {
    this.rfSegment = rfSegment;
  }

  public Integer getVerificationStatusId() {
    return verificationStatusId;
  }

  public void setVerificationStatusId(Integer verificationStatusId) {
    this.verificationStatusId = verificationStatusId;
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

  public String getExternalUrl() {
    return externalUrl;
  }

  public void setExternalUrl(String externalUrl) {
    this.externalUrl = externalUrl;
  }

  public List<RFControlVerificationHistory> getRFControlVerificationHistoryList() {
    return rfControlVerificationHistoryList;
  }

  public void setRFControlVerificationHistoryList(
      List<RFControlVerificationHistory> rfControlVerificationHistoryList) {
    this.rfControlVerificationHistoryList = rfControlVerificationHistoryList;
  }

  public CreditedControl getCreditedControl() {
    return creditedControl;
  }

  public void setCreditedControl(CreditedControl creditedControl) {
    this.creditedControl = creditedControl;
  }

  public List<Component> getComponentList() {
    return componentList;
  }

  public void setComponentList(List<Component> componentList) {
    this.componentList = componentList;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (rfControlVerificationId != null ? rfControlVerificationId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof RFControlVerification)) {
      return false;
    }
    RFControlVerification other = (RFControlVerification) object;
    return (this.rfControlVerificationId != null || other.rfControlVerificationId == null)
        && (this.rfControlVerificationId == null
            || this.rfControlVerificationId.equals(other.rfControlVerificationId));
  }

  @Override
  public String toString() {
    return "org.jlab.jam.persistence.entity.RFControlVerification[ rfControlVerificationId="
        + rfControlVerificationId
        + " ]";
  }

  @Override
  public int compareTo(RFControlVerification o) {
    return this.rfSegment.getWeight().compareTo(o.rfSegment.getWeight());
  }
}
