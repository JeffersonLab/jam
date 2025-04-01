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
import org.jlab.jam.persistence.entity.VerificationTeam;
import org.jlab.smoothness.business.service.UserAuthorizationService;
import org.jlab.smoothness.persistence.view.User;

/**
 * @author ryans
 */
@Stateless
public class VerificationTeamFacade extends AbstractFacade<VerificationTeam> {
  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public VerificationTeamFacade() {
    super(VerificationTeam.class);
  }

  @PermitAll
  public List<VerificationTeam> findWithControlsAndUsers(String name) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<VerificationTeam> cq = cb.createQuery(VerificationTeam.class);
    Root<VerificationTeam> root = cq.from(VerificationTeam.class);

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
    TypedQuery<VerificationTeam> q = getEntityManager().createQuery(cq);

    List<VerificationTeam> teamList = q.getResultList();

    for (VerificationTeam team : teamList) {
      Collections.sort(team.getControlList());
      UserAuthorizationService userService = UserAuthorizationService.getInstance();
      List<User> userList = userService.getUsersInRole(team.getDirectoryRoleName());
      team.setUserList(userList);
    }

    return teamList;
  }
}
