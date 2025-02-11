package org.jlab.jam.persistence.entity;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

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
