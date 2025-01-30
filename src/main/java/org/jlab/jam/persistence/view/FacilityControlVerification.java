package org.jlab.jam.persistence.view;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author ryans
 */
@Entity
@Table(name = "FACILITY_CONTROL_VERIFICATION", schema = "JAM_OWNER")
public class FacilityControlVerification implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "FACILITY_ID", nullable = false)
  private BigInteger facilityId;

  @Column(name = "CREDITED_CONTROL_ID")
  private BigInteger creditedControlId;

  @Column(name = "VERIFICATION_STATUS_ID")
  private Integer verificationStatusId;

  @Column(name = "EXPIRATION_DATE")
  @Temporal(TemporalType.TIMESTAMP)
  private Date expirationDate;

  public FacilityControlVerification() {}

  public BigInteger getFacilityId() {
    return facilityId;
  }

  public BigInteger getCreditedControlId() {
    return creditedControlId;
  }

  public Integer getVerificationStatusId() {
    return verificationStatusId;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }
}
