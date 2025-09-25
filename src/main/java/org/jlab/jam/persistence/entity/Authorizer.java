package org.jlab.jam.persistence.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * @author ryans
 */
@Entity
@Table(name = "AUTHORIZER", schema = "JAM_OWNER")
public class Authorizer implements Serializable {

  private static final long serialVersionUID = 1L;
  @EmbeddedId protected AuthorizerPK authorizerPK;

  public AuthorizerPK getAuthorizerPK() {
    return authorizerPK;
  }

  public void setAuthorizerPK(AuthorizerPK authorizerPK) {
    this.authorizerPK = authorizerPK;
  }
}
