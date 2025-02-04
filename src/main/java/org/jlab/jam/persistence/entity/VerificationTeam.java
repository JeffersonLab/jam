package org.jlab.jam.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.jlab.smoothness.persistence.view.User;

/**
 * @author ryans
 */
@Entity
@Table(name = "VERIFICATION_TEAM", schema = "JAM_OWNER")
public class VerificationTeam implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "VERIFICATION_TEAM_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger workgroupId;

  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 64)
  @Column(nullable = false, length = 64)
  private String name;

  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 64)
  @Column(name = "DIRECTORY_ROLE_NAME", nullable = false, length = 64)
  private String leaderRoleName;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "group")
  private List<CreditedControl> controlList;

  @Transient private List<User> leaders;

  public BigInteger getWorkgroupId() {
    return workgroupId;
  }

  public void setWorkgroupId(BigInteger groupId) {
    this.workgroupId = groupId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLeaderRoleName() {
    return leaderRoleName;
  }

  public List<CreditedControl> getControlList() {
    return controlList;
  }

  public void setControlList(List<CreditedControl> controlList) {
    this.controlList = controlList;
  }

  public void setLeaderRoleName(String leaderRoleName) {
    this.leaderRoleName = leaderRoleName;
  }

  public void setLeaders(List<User> leaders) {
    this.leaders = leaders;
  }

  public List<User> getLeaders() {
    return leaders;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 97 * hash + (this.workgroupId != null ? this.workgroupId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final VerificationTeam other = (VerificationTeam) obj;
    return Objects.equals(this.workgroupId, other.workgroupId);
  }
}
