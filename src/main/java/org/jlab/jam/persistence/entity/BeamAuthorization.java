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
@Table(name = "BEAM_AUTHORIZATION", schema = "JAM_OWNER")
@NamedQueries({
  @NamedQuery(name = "BeamAuthorization.findAll", query = "SELECT a FROM BeamAuthorization a")
})
public class BeamAuthorization implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(
      name = "BeamAuthorizationId",
      sequenceName = "BEAM_AUTHORIZATION_ID",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BeamAuthorizationId")
  @Basic(optional = false)
  @NotNull
  @Column(name = "BEAM_AUTHORIZATION_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger beamAuthorizationId;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "FACILITY_ID", referencedColumnName = "FACILITY_ID", nullable = false)
  private Facility facility;

  @Basic(optional = false)
  @NotNull
  @Column(name = "AUTHORIZATION_DATE", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date authorizationDate;

  @Basic(optional = false)
  @NotNull
  @Column(name = "MODIFIED_DATE", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date modifiedDate;

  @NotNull
  @Column(name = "AUTHORIZED_BY", nullable = false)
  private String authorizedBy;

  @NotNull
  @Column(name = "MODIFIED_BY", nullable = false)
  private String modifiedBy;

  @Size(max = 2048)
  @Column(length = 2048)
  private String comments;

  @Size(max = 2048)
  @Column(name = "LOGENTRY_URL", length = 2048)
  private String logentryUrl;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "beamAuthorization", fetch = FetchType.EAGER)
  private List<BeamDestinationAuthorization> beamDestinationAuthorizationList;

  public BeamAuthorization() {}

  public BigInteger getBeamAuthorizationId() {
    return beamAuthorizationId;
  }

  public void setBeamAuthorizationId(BigInteger beamAuthorizationId) {
    this.beamAuthorizationId = beamAuthorizationId;
  }

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public Date getAuthorizationDate() {
    return authorizationDate;
  }

  public void setAuthorizationDate(Date authorizationDate) {
    this.authorizationDate = authorizationDate;
  }

  public Date getModifiedDate() {
    return modifiedDate;
  }

  public void setModifiedDate(Date modifiedDate) {
    this.modifiedDate = modifiedDate;
  }

  public String getAuthorizedBy() {
    return authorizedBy;
  }

  public void setAuthorizedBy(String authorizedBy) {
    this.authorizedBy = authorizedBy;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public String getLogentryUrl() {
    return logentryUrl;
  }

  public void setLogentryUrl(String logentryUrl) {
    this.logentryUrl = logentryUrl;
  }

  public List<BeamDestinationAuthorization> getDestinationAuthorizationList() {
    return beamDestinationAuthorizationList;
  }

  public void setDestinationAuthorizationList(
      List<BeamDestinationAuthorization> beamDestinationAuthorizationList) {
    this.beamDestinationAuthorizationList = beamDestinationAuthorizationList;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (beamAuthorizationId != null ? beamAuthorizationId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof BeamAuthorization)) {
      return false;
    }
    BeamAuthorization other = (BeamAuthorization) object;
    return (this.beamAuthorizationId != null || other.beamAuthorizationId == null)
        && (this.beamAuthorizationId == null
            || this.beamAuthorizationId.equals(other.beamAuthorizationId));
  }

  @Override
  public String toString() {
    return "org.jlab.jam.persistence.entity.BeamAuthorization[beamAuthorizationId="
        + beamAuthorizationId
        + " ]";
  }

  public BeamAuthorization createAdminClone() {
    BeamAuthorization other = new BeamAuthorization();
    other.facility = facility;
    other.authorizationDate = this.authorizationDate;
    other.authorizedBy = this.authorizedBy;
    other.comments = this.comments;
    other.setModifiedBy("jam-admin");
    other.setModifiedDate(new Date());
    return other;
  }
}
