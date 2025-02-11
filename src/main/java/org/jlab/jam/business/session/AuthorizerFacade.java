package org.jlab.jam.business.session;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
    TypedQuery<Authorizer> q = getEntityManager().createQuery(cq);
    return q.getResultList();
  }
}
