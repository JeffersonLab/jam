package org.jlab.jam.business.session;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import org.jlab.jam.persistence.entity.Authorizer;
import org.jlab.jam.persistence.entity.Facility;
import org.jlab.jam.persistence.enumeration.OperationsType;

/**
 * @author ryans
 */
@Stateless
public class AuthorizerFacade extends AbstractFacade<Authorizer> {
  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public AuthorizerFacade() {
    super(Authorizer.class);
  }

  @PermitAll
  public List<Authorizer> filterList(Facility facility, OperationsType type) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Authorizer> cq = cb.createQuery(Authorizer.class);
    Root<Authorizer> root = cq.from(Authorizer.class);

    List<Predicate> filters = new ArrayList<>();

    if (facility != null) {
      filters.add(cb.equal(root.get("authorizerPK").get("facility"), facility));
    }

    if (type != null) {
      filters.add(cb.equal(root.get("authorizerPK").get("operationsType"), type));
    }

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }

    cq.select(root);

    List<Order> orders = new ArrayList<>();

    Path p0 = root.get("authorizerPK").get("facility").get("weight");
    Order o0 = cb.asc(p0);
    orders.add(o0);

    Path p1 = root.get("authorizerPK").get("operationsType");
    Order o1 = cb.asc(p1);
    orders.add(o1);

    Path p2 = root.get("authorizerPK").get("username");
    Order o2 = cb.asc(p2);
    orders.add(o2);

    cq.orderBy(orders);

    TypedQuery<Authorizer> q = getEntityManager().createQuery(cq);
    return q.getResultList();
  }
}
