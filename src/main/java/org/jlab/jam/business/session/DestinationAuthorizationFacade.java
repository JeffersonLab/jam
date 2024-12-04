package org.jlab.jam.business.session;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jlab.jam.persistence.entity.DestinationAuthorization;

/**
 * @author ryans
 */
@Stateless
public class DestinationAuthorizationFacade extends AbstractFacade<DestinationAuthorization> {
  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public DestinationAuthorizationFacade() {
    super(DestinationAuthorization.class);
  }
}
