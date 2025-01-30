package org.jlab.jam.persistence.view;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import org.jlab.jam.persistence.entity.CreditedControl;
import org.jlab.jam.persistence.entity.Facility;

/**
 * @author ryans
 */
@Embeddable
public class FacilityControlVerificationPK
    implements Serializable, Comparable<FacilityControlVerificationPK> {
  @NotNull
  @ManyToOne
  @JoinColumn(name = "FACILITY_ID", referencedColumnName = "FACILITY_ID", nullable = false)
  private Facility facility;

  @NotNull
  @ManyToOne
  @JoinColumn(
      name = "CREDITED_CONTROL_ID",
      referencedColumnName = "CREDITED_CONTROL_ID",
      nullable = false)
  private CreditedControl creditedControl;

  public FacilityControlVerificationPK() {}

  public FacilityControlVerificationPK(Facility facility, CreditedControl creditedControl) {
    this.facility = facility;
    this.creditedControl = creditedControl;
  }

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public CreditedControl getCreditedControl() {
    return creditedControl;
  }

  public void setCreditedControl(CreditedControl creditedControl) {
    this.creditedControl = creditedControl;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof FacilityControlVerificationPK)) return false;
    FacilityControlVerificationPK that = (FacilityControlVerificationPK) o;
    return Objects.equals(facility, that.facility)
        && Objects.equals(creditedControl, that.creditedControl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(facility, creditedControl);
  }

  @Override
  public int compareTo(FacilityControlVerificationPK o) {
    int compare = this.facility.compareTo(o.facility);

    if (compare == 0) {
      compare = this.creditedControl.compareTo(o.creditedControl);
    }

    return compare;
  }
}
