package org.jlab.jam.business.session;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import org.jlab.jam.persistence.entity.Facility;
import org.jlab.jam.persistence.entity.RFSegment;

/**
 * @author ryans
 */
@Stateless
public class RFSegmentFacade extends AbstractFacade<RFSegment> {
  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public RFSegmentFacade() {
    super(RFSegment.class);
  }

  @PermitAll
  public List<RFSegment> findByFacility(Facility facility) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<RFSegment> cq = cb.createQuery(RFSegment.class);
    Root<RFSegment> root = cq.from(RFSegment.class);

    List<Predicate> filters = new ArrayList<>();

    filters.add(cb.equal(root.get("facility"), facility));

    filters.add(cb.equal(root.get("active"), true));

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }

    List<Order> orders = new ArrayList<>();
    Path p0 = root.get("weight");
    Order o0 = cb.asc(p0);
    orders.add(o0);
    cq.orderBy(orders);

    cq.select(root);
    TypedQuery<RFSegment> q = getEntityManager().createQuery(cq);

    return q.getResultList();
  }
}
