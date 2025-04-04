package org.jlab.jam.business.session;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import org.jlab.jam.persistence.entity.Facility;
import org.jlab.jam.persistence.entity.Watcher;
import org.jlab.jam.persistence.entity.WatcherPK;
import org.jlab.jam.persistence.enumeration.OperationsType;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * @author ryans
 */
@Stateless
public class WatcherFacade extends AbstractFacade<Watcher> {
  private static final Logger LOGGER = Logger.getLogger(WatcherFacade.class.getName());

  @EJB FacilityFacade facilityFacade;

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public WatcherFacade() {
    super(Watcher.class);
  }

  @PermitAll
  public List<Watcher> filterList(Facility facility, OperationsType type, String username) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Watcher> cq = cb.createQuery(Watcher.class);
    Root<Watcher> root = cq.from(Watcher.class);

    List<Predicate> filters = new ArrayList<>();

    if (facility != null) {
      filters.add(cb.equal(root.get("watcherPK").get("facility"), facility));
    }

    if (type != null) {
      filters.add(cb.equal(root.get("watcherPK").get("operationsType"), type));
    }

    if (username != null && !username.isEmpty()) {
      filters.add(cb.equal(root.get("watcherPK").get("username"), username));
    }

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }

    cq.select(root);

    List<Order> orders = new ArrayList<>();

    Path p0 = root.get("watcherPK").get("facility").get("weight");
    Order o0 = cb.asc(p0);
    orders.add(o0);

    Path p1 = root.get("watcherPK").get("operationsType");
    Order o1 = cb.asc(p1);
    orders.add(o1);

    Path p2 = root.get("watcherPK").get("username");
    Order o2 = cb.asc(p2);
    orders.add(o2);

    cq.orderBy(orders);

    TypedQuery<Watcher> q = getEntityManager().createQuery(cq);
    return q.getResultList();
  }

  @RolesAllowed("jam-admin")
  public void addWatcher(BigInteger facilityId, OperationsType type, String username)
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

    Watcher watcher = new Watcher();
    WatcherPK watcherPK = new WatcherPK();
    watcherPK.setFacility(facility);
    watcherPK.setOperationsType(type);
    watcherPK.setUsername(username);

    watcher.setWatcherPK(watcherPK);

    create(watcher);
  }

  @RolesAllowed("jam-admin")
  public void removeWatcher(BigInteger facilityId, OperationsType type, String username)
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

    List<Watcher> watcherList = filterList(facility, type, username);

    if (watcherList == null || watcherList.isEmpty()) {
      throw new UserFriendlyException("Watcher not found");
    }

    remove(watcherList.get(0));
  }
}
