package org.jlab.jam.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import org.jlab.jam.persistence.enumeration.OperationsType;

/**
 * @author ryans
 */
@Embeddable
public class WatcherPK implements Serializable {
  @NotNull
  @ManyToOne
  @JoinColumn(name = "FACILITY_ID", referencedColumnName = "FACILITY_ID", nullable = false)
  private Facility facility;

  @Basic(optional = false)
  @NotNull
  @Column(name = "OPERATIONS_TYPE", nullable = false)
  @Enumerated(EnumType.STRING)
  private OperationsType operationsType;

  @Basic(optional = false)
  @NotNull
  @Column(name = "USERNAME", nullable = false)
  private String username;

  public WatcherPK() {}

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public OperationsType getOperationsType() {
    return operationsType;
  }

  public void setOperationsType(OperationsType operationsType) {
    this.operationsType = operationsType;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof WatcherPK)) return false;
    WatcherPK that = (WatcherPK) o;
    return Objects.equals(facility, that.facility)
        && Objects.equals(operationsType, that.operationsType)
        && Objects.equals(username, that.username);
  }

  @Override
  public int hashCode() {
    return Objects.hash(facility, operationsType, username);
  }
}
