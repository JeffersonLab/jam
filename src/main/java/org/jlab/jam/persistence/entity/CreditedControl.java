package org.jlab.jam.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.jlab.jam.persistence.view.FacilityControlVerification;

/**
 * @author ryans
 */
@Entity
@Table(name = "CREDITED_CONTROL", schema = "JAM_OWNER")
@NamedQueries({
  @NamedQuery(name = "CreditedControl.findAll", query = "SELECT c FROM CreditedControl c")
})
public class CreditedControl implements Serializable, Comparable<CreditedControl> {

  private static final long serialVersionUID = 1L;

  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "CREDITED_CONTROL_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger creditedControlId;

  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 128)
  @Column(nullable = false, length = 128)
  private String name;

  @Size(max = 2048)
  @Column(length = 2048)
  private String description;

  @NotNull
  @JoinColumn(
      name = "VERIFICATION_TEAM_ID",
      referencedColumnName = "VERIFICATION_TEAM_ID",
      nullable = false)
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  private VerificationTeam verificationTeam;

  private BigInteger weight;

  @Column(name = "VERIFICATION_FREQUENCY", nullable = true, length = 128)
  @Size(min = 0, max = 128)
  private String verificationFrequency;

  @Size(max = 2048)
  @Column(length = 2048)
  private String comments;

  @OneToMany(mappedBy = "creditedControl", fetch = FetchType.LAZY)
  private List<BeamControlVerification> beamControlVerificationList;

  @OneToMany(mappedBy = "creditedControl", fetch = FetchType.LAZY)
  private List<RFControlVerification> rfControlVerificationList;

  @OneToMany(mappedBy = "facilityControlVerificationPK.creditedControl", fetch = FetchType.LAZY)
  private List<FacilityControlVerification> facilityControlVerificationList;

  public CreditedControl() {}

  public CreditedControl(BigInteger creditedControlId) {
    this.creditedControlId = creditedControlId;
  }

  public BigInteger getCreditedControlId() {
    return creditedControlId;
  }

  public void setCreditedControlId(BigInteger creditedControlId) {
    this.creditedControlId = creditedControlId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public VerificationTeam getVerificationTeam() {
    return verificationTeam;
  }

  public void setVerificationTeam(VerificationTeam verificationTeam) {
    this.verificationTeam = verificationTeam;
  }

  public BigInteger getWeight() {
    return weight;
  }

  public void setWeight(BigInteger weight) {
    this.weight = weight;
  }

  public String getVerificationFrequency() {
    return verificationFrequency;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public void setVerificationFrequency(String verificationFrequency) {
    this.verificationFrequency = verificationFrequency;
  }

  public List<RFControlVerification> getRFControlVerificationList() {
    return rfControlVerificationList;
  }

  public void setRFControlVerificationList(List<RFControlVerification> rfControlVerificationList) {
    this.rfControlVerificationList = rfControlVerificationList;
  }

  public List<BeamControlVerification> getBeamControlVerificationList() {
    return beamControlVerificationList;
  }

  public void setBeamControlVerificationList(
      List<BeamControlVerification> beamControlVerificationList) {
    this.beamControlVerificationList = beamControlVerificationList;
  }

  public List<FacilityControlVerification> getFacilityControlVerificationList() {
    return facilityControlVerificationList;
  }

  public boolean hasRFSegment(RFSegment segment) {
    boolean hasSegment = false;
    if (beamControlVerificationList != null) {
      for (RFControlVerification verification : rfControlVerificationList) {
        if (verification.getRFSegment().equals(segment)) {
          hasSegment = true;
          break;
        }
      }
    }

    return hasSegment;
  }

  public boolean hasBeamDestination(BeamDestination destination) {
    boolean hasDestination = false;
    if (beamControlVerificationList != null) {
      for (BeamControlVerification verification : beamControlVerificationList) {
        if (verification.getBeamDestination().equals(destination)) {
          hasDestination = true;
          break;
        }
      }
    }

    return hasDestination;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (creditedControlId != null ? creditedControlId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof CreditedControl)) {
      return false;
    }
    CreditedControl other = (CreditedControl) object;
    return (this.creditedControlId != null || other.creditedControlId == null)
        && (this.creditedControlId == null
            || this.creditedControlId.equals(other.creditedControlId));
  }

  @Override
  public String toString() {
    return "org.jlab.jam.persistence.entity.CreditedControl[ creditedControlId="
        + creditedControlId
        + " ]";
  }

  @Override
  public int compareTo(CreditedControl o) {
    // return this.getWeight().compareTo(o.getWeight());

    return (this.getWeight() == null ? BigInteger.ZERO : this.getWeight())
        .compareTo(o.getWeight() == null ? BigInteger.ZERO : o.getWeight());

    /*int result = this.workgroup.getName().compareTo(o.getWorkgroup().getName());

    if(result == 0) {
        result = this.getName().compareTo(o.getName());
    }

    return result;*/
  }
}
