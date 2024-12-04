package org.jlab.bam.business.session;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jlab.bam.persistence.entity.Workgroup;

/**
 * @author ryans
 */
@Stateless
public class WorkgroupFacade extends AbstractFacade<Workgroup> {
  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public WorkgroupFacade() {
    super(Workgroup.class);
  }
}
