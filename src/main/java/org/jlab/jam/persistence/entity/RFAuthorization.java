package org.jlab.jam.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "RF_AUTHORIZATION", schema = "JAM_OWNER")
@NamedQueries({
  @NamedQuery(name = "RFAuthorization.findAll", query = "SELECT a FROM RFAuthorization a")
})
public class RFAuthorization implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(
      name = "RFAuthorizationId",
      sequenceName = "RF_AUTHORIZATION_ID",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RFAuthorizationId")
  @Basic(optional = false)
  @NotNull
  @Column(name = "RF_AUTHORIZATION_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger rfAuthorizationId;

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

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "rfAuthorization", fetch = FetchType.EAGER)
  private List<RFSegmentAuthorization> rfSegmentAuthorizationList;

  public RFAuthorization() {}

  public BigInteger getRfAuthorizationId() {
    return rfAuthorizationId;
  }

  public void setRfAuthorizationId(BigInteger rfAuthorizationId) {
    this.rfAuthorizationId = rfAuthorizationId;
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

  public List<RFSegmentAuthorization> getRFSegmentAuthorizationList() {
    return rfSegmentAuthorizationList;
  }

  public void setRFSegmentAuthorizationList(
      List<RFSegmentAuthorization> rfSegmentAuthorizationList) {
    this.rfSegmentAuthorizationList = rfSegmentAuthorizationList;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (rfAuthorizationId != null ? rfAuthorizationId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof RFAuthorization)) {
      return false;
    }
    RFAuthorization other = (RFAuthorization) object;
    return (this.rfAuthorizationId != null || other.rfAuthorizationId == null)
        && (this.rfAuthorizationId == null
            || this.rfAuthorizationId.equals(other.rfAuthorizationId));
  }

  @Override
  public String toString() {
    return "org.jlab.jam.persistence.entity.RFAuthorization[rfAuthorizationId="
        + rfAuthorizationId
        + " ]";
  }

  public RFAuthorization createAdminClone() {
    RFAuthorization other = new RFAuthorization();
    other.authorizationDate = this.authorizationDate;
    other.authorizedBy = this.authorizedBy;
    other.comments = this.comments;
    other.setModifiedBy("jam-admin");
    other.setModifiedDate(new Date());
    return other;
  }
}
