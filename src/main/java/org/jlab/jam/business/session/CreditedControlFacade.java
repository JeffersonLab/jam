package org.jlab.jam.business.session;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.jlab.jam.persistence.entity.CreditedControl;
import org.jlab.smoothness.persistence.util.JPAUtil;

/**
 * @author ryans
 */
@Stateless
public class CreditedControlFacade extends AbstractFacade<CreditedControl> {

  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public CreditedControlFacade() {
    super(CreditedControl.class);
  }

  @PermitAll
  public CreditedControl findWithVerificationList(BigInteger creditedControlId) {
    TypedQuery<CreditedControl> q =
        em.createQuery(
            "select a from CreditedControl a JOIN FETCH a.controlVerificationList as b where b.beamDestination.active = true and a.creditedControlId = :creditedControlId",
            CreditedControl.class);

    q.setParameter("creditedControlId", creditedControlId);

    List<CreditedControl> ccList = q.getResultList();

    CreditedControl cc = null;

    if (ccList != null && !ccList.isEmpty()) {
      cc = ccList.get(0);

      // JPAUtil.initialize(cc.getControlVerificationList());
      Collections.sort(cc.getControlVerificationList());
    }

    return cc;
  }

  @PermitAll
  public List<CreditedControl> findAllWithVerificationList() {
    TypedQuery<CreditedControl> q =
        em.createQuery(
            "select a from CreditedControl a order by a.weight asc", CreditedControl.class);

    List<CreditedControl> ccList = q.getResultList();

    if (ccList != null) {
      for (CreditedControl cc : ccList) {
        JPAUtil.initialize(cc.getControlVerificationList());
        // Collections.sort(cc.getControlVerificationList());
      }
    }

    return ccList;
  }

  @RolesAllowed("jam-admin")
  public void updateComments(BigInteger creditedControlId, String comments) {
    CreditedControl control = find(creditedControlId);

    control.setComments(comments);
  }
}
