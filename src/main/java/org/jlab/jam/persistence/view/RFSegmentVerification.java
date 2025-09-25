package org.jlab.jam.persistence.view;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

/**
 * @author ryans
 */
@Entity
@Table(name = "RF_SEGMENT_VERIFICATION", schema = "JAM_OWNER")
public class RFSegmentVerification implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "RF_SEGMENT_ID", nullable = false)
  private BigInteger rfSegmentId;

  @Column(name = "VERIFICATION_STATUS_ID")
  private Integer verificationStatusId;

  @Column(name = "EXPIRATION_DATE")
  @Temporal(TemporalType.TIMESTAMP)
  private Date expirationDate;

  public RFSegmentVerification() {}

  public BigInteger getRFSegmentId() {
    return rfSegmentId;
  }

  public Integer getVerificationStatusId() {
    return verificationStatusId;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }
}
