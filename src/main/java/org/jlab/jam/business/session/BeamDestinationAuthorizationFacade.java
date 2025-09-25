package org.jlab.jam.business.session;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jlab.jam.persistence.entity.BeamDestinationAuthorization;

/**
 * @author ryans
 */
@Stateless
public class BeamDestinationAuthorizationFacade
    extends AbstractFacade<BeamDestinationAuthorization> {
  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public BeamDestinationAuthorizationFacade() {
    super(BeamDestinationAuthorization.class);
  }
}
