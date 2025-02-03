package org.jlab.jam.business.session;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jlab.jam.persistence.entity.BeamDestinationAuthorization;

/**
 * @author ryans
 */
@Stateless
public class BeamDestinationAuthorizationFacade
    extends AbstractFacade<BeamDestinationAuthorization> {
  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public BeamDestinationAuthorizationFacade() {
    super(BeamDestinationAuthorization.class);
  }
}
