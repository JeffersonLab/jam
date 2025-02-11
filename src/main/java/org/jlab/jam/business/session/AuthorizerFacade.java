package org.jlab.jam.business.session;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import org.jlab.jam.persistence.entity.Authorizer;
import org.jlab.jam.persistence.entity.AuthorizerPK;
import org.jlab.jam.persistence.entity.Facility;
import org.jlab.jam.persistence.enumeration.OperationsType;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * @author ryans
 */
@Stateless
public class AuthorizerFacade extends AbstractFacade<Authorizer> {
  @EJB FacilityFacade facilityFacade;

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
  public List<Authorizer> filterList(Facility facility, OperationsType type, String username) {
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

    if (username != null && !username.isEmpty()) {
      filters.add(cb.equal(root.get("authorizerPK").get("username"), username));
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

  @RolesAllowed("jam-admin")
  public void addAuthorizer(BigInteger facilityId, OperationsType type, String username)
      throws UserFriendlyException {

    if (facilityId == null) {
      throw new UserFriendlyException("facilityId is required");
    }

    Facility facility = facilityFacade.find(facilityId);

    if (facility == null) {
      throw new UserFriendlyException("facility with ID " + facilityId + " not found");
    }

    if (type == null) {
      throw new UserFriendlyException("type is required");
    }

    if (username == null) {
      throw new UserFriendlyException("username is required");
    }

    Authorizer authorizer = new Authorizer();
    AuthorizerPK authorizerPK = new AuthorizerPK();
    authorizerPK.setFacility(facility);
    authorizerPK.setOperationsType(type);
    authorizerPK.setUsername(username);

    authorizer.setAuthorizerPK(authorizerPK);

    create(authorizer);
  }

  @RolesAllowed("jam-admin")
  public void removeAuthorizer(BigInteger facilityId, OperationsType type, String username)
      throws UserFriendlyException {
    if (facilityId == null) {
      throw new UserFriendlyException("facilityId is required");
    }

    Facility facility = facilityFacade.find(facilityId);

    if (facility == null) {
      throw new UserFriendlyException("facility with ID " + facilityId + " not found");
    }

    if (type == null) {
      throw new UserFriendlyException("type is required");
    }

    if (username == null) {
      throw new UserFriendlyException("username is required");
    }

    List<Authorizer> authorizerList = filterList(facility, type, username);

    if (authorizerList == null || authorizerList.isEmpty()) {
      throw new UserFriendlyException("Authorizer not found");
    }

    remove(authorizerList.get(0));
  }

  @PermitAll
  public void isAuthorizer(Facility facility, OperationsType type, String username)
      throws UserFriendlyException {
    List<Authorizer> authorizerList = filterList(facility, type, username);

    if (authorizerList == null || authorizerList.isEmpty()) {
      throw new UserFriendlyException("Not Authorized to Authorize!");
    }
  }
}
