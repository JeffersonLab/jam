package org.jlab.jam.business.session;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import org.jlab.jam.persistence.entity.BeamControlVerification;
import org.jlab.jam.persistence.entity.BeamDestination;
import org.jlab.jam.persistence.entity.Facility;

/**
 * @author ryans
 */
@Stateless
public class BeamDestinationFacade extends AbstractFacade<BeamDestination> {
  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public BeamDestinationFacade() {
    super(BeamDestination.class);
  }

  @PermitAll
  public BeamDestination findWithVerificationList(BigInteger destinationId) {
    TypedQuery<BeamDestination> q =
        em.createQuery(
            "select a from BeamDestination a where a.beamDestinationId = :destinationId",
            BeamDestination.class);

    q.setParameter("destinationId", destinationId);

    List<BeamDestination> destinationList = q.getResultList();

    BeamDestination destination = null;

    if (destinationList != null && !destinationList.isEmpty()) {
      destination = destinationList.get(0);

      // JPAUtil.initialize(destination.getControlVerificationList());
      for (BeamControlVerification verification : destination.getBeamControlVerificationList()) {
        verification.getCreditedControl().getName();
      }

      Collections.sort(
          destination.getBeamControlVerificationList(),
          new Comparator<BeamControlVerification>() {
            @Override
            public int compare(BeamControlVerification o1, BeamControlVerification o2) {
              return o1.getCreditedControl().compareTo(o2.getCreditedControl());
            }
          });
    }

    return destination;
  }

  @PermitAll
  public List<BeamDestination> filterList(Boolean active, Facility facility) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<BeamDestination> cq = cb.createQuery(BeamDestination.class);
    Root<BeamDestination> root = cq.from(BeamDestination.class);

    List<Predicate> filters = new ArrayList<>();

    if (active != null) {
      filters.add(cb.equal(root.get("active"), active));
    }

    if (facility != null) {
      filters.add(cb.equal(root.get("facility"), facility));
    }

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }

    List<Order> orders = new ArrayList<>();

    Path p0 = root.get("facility");
    Order o0 = cb.asc(p0);
    orders.add(o0);

    Path p1 = root.get("weight");
    Order o1 = cb.asc(p1);
    orders.add(o1);

    cq.orderBy(orders);

    cq.select(root);
    TypedQuery<BeamDestination> q = getEntityManager().createQuery(cq);

    return q.getResultList();
  }
}
