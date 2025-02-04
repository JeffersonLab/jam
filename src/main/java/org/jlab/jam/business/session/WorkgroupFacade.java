package org.jlab.jam.business.session;

import java.util.Collections;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
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
  public List<Workgroup> findWithControlsAndUsers() {

    TypedQuery<Workgroup> q =
        em.createQuery("select a from Workgroup a order by a.name asc", Workgroup.class);

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
