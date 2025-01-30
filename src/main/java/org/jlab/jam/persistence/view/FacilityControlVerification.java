package org.jlab.jam.persistence.view;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 * @author ryans
 */
@Entity
@Table(name = "FACILITY_CONTROL_VERIFICATION", schema = "JAM_OWNER")
public class FacilityControlVerification implements Serializable {

  private static final long serialVersionUID = 1L;

  @EmbeddedId protected FacilityControlVerificationPK facilityControlVerificationPK;

  @Column(name = "VERIFICATION_STATUS_ID")
  private Integer verificationStatusId;

  @Column(name = "EXPIRATION_DATE")
  @Temporal(TemporalType.TIMESTAMP)
  private Date expirationDate;

  public FacilityControlVerification() {}

  public FacilityControlVerificationPK getFacilityControlVerificationPK() {
    return facilityControlVerificationPK;
  }

  public void setFacilityControlVerificationPK(
      FacilityControlVerificationPK facilityControlVerificationPK) {
    this.facilityControlVerificationPK = facilityControlVerificationPK;
  }

  public Integer getVerificationStatusId() {
    return verificationStatusId;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }
}
