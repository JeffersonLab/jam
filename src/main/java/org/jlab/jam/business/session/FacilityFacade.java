package org.jlab.jam.business.session;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import org.jlab.jam.persistence.entity.Facility;

/**
 * @author ryans
 */
@Stateless
public class FacilityFacade extends AbstractFacade<Facility> {
  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public FacilityFacade() {
    super(Facility.class);
  }

  @PermitAll
  public Facility findByPath(String pathInfo) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Facility> cq = cb.createQuery(Facility.class);
    Root<Facility> root = cq.from(Facility.class);

    List<Predicate> filters = new ArrayList<>();

    filters.add(cb.equal(root.get("path"), pathInfo));

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }

    cq.select(root);
    TypedQuery<Facility> q = getEntityManager().createQuery(cq).setFirstResult(0).setMaxResults(2);
    List<Facility> recordList = q.getResultList();

    Facility facility = null;

    if (recordList.size() > 1) {
      throw new RuntimeException("Duplicate facility paths configured");
    }

    if (recordList.size() > 0) {
      facility = recordList.get(0);
    }

    return facility;
  }
}
