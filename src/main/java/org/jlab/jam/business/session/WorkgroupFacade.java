package org.jlab.jam.business.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import org.jlab.jam.persistence.entity.Workgroup;
import org.jlab.smoothness.business.service.UserAuthorizationService;
import org.jlab.smoothness.persistence.view.User;

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

  @PermitAll
  public List<Workgroup> findWithControlsAndUsers(String name) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Workgroup> cq = cb.createQuery(Workgroup.class);
    Root<Workgroup> root = cq.from(Workgroup.class);

    List<Predicate> filters = new ArrayList<>();

    if (name != null) {
      filters.add(cb.equal(root.get("name"), name));
    }

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }

    List<Order> orders = new ArrayList<>();

    Path p0 = root.get("name");
    Order o0 = cb.asc(p0);
    orders.add(o0);

    cq.orderBy(orders);

    cq.select(root);
    TypedQuery<Workgroup> q = getEntityManager().createQuery(cq);

    List<Workgroup> teamList = q.getResultList();

    for (Workgroup team : teamList) {
      Collections.sort(team.getControlList());
      UserAuthorizationService userService = UserAuthorizationService.getInstance();
      List<User> userList = userService.getUsersInRole(team.getLeaderRoleName());
      team.setLeaders(userList);
    }

    return teamList;
  }
}
