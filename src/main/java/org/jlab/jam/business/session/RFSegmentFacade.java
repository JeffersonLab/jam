package org.jlab.jam.business.session;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.jlab.jam.persistence.entity.*;

/**
 * @author ryans
 */
@Stateless
public class RFSegmentFacade extends AbstractFacade<RFSegment> {
  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public RFSegmentFacade() {
    super(RFSegment.class);
  }

  @PermitAll
  public RFSegment findWithVerificationList(BigInteger segmentId) {
    TypedQuery<RFSegment> q =
        em.createQuery(
            "select a from RFSegment a where a.rfSegmentId = :segmentId", RFSegment.class);

    q.setParameter("segmentId", segmentId);

    List<RFSegment> segmentList = q.getResultList();

    RFSegment segment = null;

    if (segmentList != null && !segmentList.isEmpty()) {
      segment = segmentList.get(0);

      // JPAUtil.initialize(destination.getControlVerificationList());
      for (RFControlVerification verification : segment.getRFControlVerificationList()) {
        verification.getCreditedControl().getName();
      }

      Collections.sort(
          segment.getRFControlVerificationList(),
          new Comparator<RFControlVerification>() {
            @Override
            public int compare(RFControlVerification o1, RFControlVerification o2) {
              return o1.getCreditedControl().compareTo(o2.getCreditedControl());
            }
          });
    }

    return segment;
  }

  @PermitAll
  public List<RFSegment> filterList(Boolean active, Facility facility, VerificationTeam team) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<RFSegment> cq = cb.createQuery(RFSegment.class);
    Root<RFSegment> root = cq.from(RFSegment.class);

    List<Predicate> filters = new ArrayList<>();

    if (facility != null) {
      filters.add(cb.equal(root.get("facility"), facility));
    }

    if (active != null) {
      filters.add(cb.equal(root.get("active"), active));
    }

    if (team != null) {
      Subquery<BigInteger> subquery = cq.subquery(BigInteger.class);
      Root<RFControlVerification> subRoot = subquery.from(RFControlVerification.class);
      Join<RFControlVerification, RFSegment> segment = subRoot.join("rfSegment");
      Join<RFControlVerification, CreditedControl> control = subRoot.join("creditedControl");
      subquery.select(segment.get("rfSegmentId"));
      subquery.where(cb.equal(control.get("verificationTeam"), team));
      filters.add(cb.in(root.get("rfSegmentId")).value(subquery));
    }

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }

    List<Order> orders = new ArrayList<>();

    Path p1 = root.get("facility");
    Order o1 = cb.asc(p1);
    orders.add(o1);

    Path p0 = root.get("weight");
    Order o0 = cb.asc(p0);
    orders.add(o0);

    cq.orderBy(orders);

    cq.select(root);
    TypedQuery<RFSegment> q = getEntityManager().createQuery(cq);

    return q.getResultList();
  }
}
