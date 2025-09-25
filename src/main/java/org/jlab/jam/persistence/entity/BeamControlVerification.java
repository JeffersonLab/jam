package org.jlab.jam.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * @author ryans
 */
@Entity
@Table(name = "BEAM_CONTROL_VERIFICATION", schema = "JAM_OWNER")
@NamedQueries({
  @NamedQuery(
      name = "BeamControlVerification.findAll",
      query = "SELECT c FROM BeamControlVerification c")
})
public class BeamControlVerification implements Serializable, Comparable<BeamControlVerification> {
  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(
      name = "BeamControlVerificationId",
      sequenceName = "BEAM_CONTROL_VERIFICATION_ID",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BeamControlVerificationId")
  @Basic(optional = false)
  @NotNull
  @Column(name = "BEAM_CONTROL_VERIFICATION_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger beamControlVerificationId;

  @NotNull
  @JoinColumn(name = "BEAM_DESTINATION_ID", referencedColumnName = "BEAM_DESTINATION_ID")
  @ManyToOne(fetch = FetchType.EAGER)
  private BeamDestination beamDestination;

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

  @OneToMany(mappedBy = "beamControlVerification", fetch = FetchType.LAZY)
  private List<BeamControlVerificationHistory> beamControlVerificationHistoryList;

  @JoinColumn(name = "CREDITED_CONTROL_ID", referencedColumnName = "CREDITED_CONTROL_ID")
  @ManyToOne(fetch = FetchType.LAZY)
  private CreditedControl creditedControl;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "BEAM_CONTROL_VERIFICATION_COMPONENT",
      joinColumns = @JoinColumn(name = "BEAM_CONTROL_VERIFICATION_ID"),
      inverseJoinColumns = @JoinColumn(name = "COMPONENT_ID"))
  private List<Component> componentList;

  public BeamControlVerification() {}

  public BigInteger getBeamControlVerificationId() {
    return beamControlVerificationId;
  }

  public void setBeamControlVerificationId(BigInteger beamControlVerificationId) {
    this.beamControlVerificationId = beamControlVerificationId;
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

  public List<BeamControlVerificationHistory> getVerificationHistoryList() {
    return beamControlVerificationHistoryList;
  }

  public void setVerificationHistoryList(
      List<BeamControlVerificationHistory> beamControlVerificationHistoryList) {
    this.beamControlVerificationHistoryList = beamControlVerificationHistoryList;
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
    hash += (beamControlVerificationId != null ? beamControlVerificationId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof BeamControlVerification)) {
      return false;
    }
    BeamControlVerification other = (BeamControlVerification) object;
    return (this.beamControlVerificationId != null || other.beamControlVerificationId == null)
        && (this.beamControlVerificationId == null
            || this.beamControlVerificationId.equals(other.beamControlVerificationId));
  }

  @Override
  public String toString() {
    return "org.jlab.jam.persistence.entity.BeamControlVerification[ beamControlVerificationId="
        + beamControlVerificationId
        + " ]";
  }

  @Override
  public int compareTo(BeamControlVerification o) {
    return this.beamDestination.getWeight().compareTo(o.beamDestination.getWeight());
  }
}
