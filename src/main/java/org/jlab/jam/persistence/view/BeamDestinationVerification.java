package org.jlab.jam.persistence.view;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

/**
 * @author ryans
 */
@Entity
@Table(name = "BEAM_DESTINATION_VERIFICATION", schema = "JAM_OWNER")
public class BeamDestinationVerification implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "BEAM_DESTINATION_ID", nullable = false)
  private BigInteger beamDestinationId;

  @Column(name = "VERIFICATION_STATUS_ID")
  private Integer verificationStatusId;

  @Column(name = "EXPIRATION_DATE")
  @Temporal(TemporalType.TIMESTAMP)
  private Date expirationDate;

  public BeamDestinationVerification() {}

  public BigInteger getBeamDestinationId() {
    return beamDestinationId;
  }

  public Integer getVerificationStatusId() {
    return verificationStatusId;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }
}
