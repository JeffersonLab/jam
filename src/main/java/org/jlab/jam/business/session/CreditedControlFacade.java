package org.jlab.jam.business.session;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import org.jlab.jam.persistence.entity.CreditedControl;
import org.jlab.jam.persistence.entity.Facility;
import org.jlab.jam.persistence.view.FacilityControlVerification;
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
  public CreditedControl findWithVerificationListTrio(BigInteger creditedControlId) {
    TypedQuery<CreditedControl> q =
        em.createQuery(
            "select a from CreditedControl a where a.creditedControlId = :creditedControlId",
            CreditedControl.class);

    q.setParameter("creditedControlId", creditedControlId);

    List<CreditedControl> ccList = q.getResultList();

    CreditedControl cc = null;

    if (ccList != null && !ccList.isEmpty()) {
      cc = ccList.get(0);

      // JPAUtil.initialize(cc.getControlVerificationList());
      Collections.sort(cc.getBeamControlVerificationList());
      Collections.sort(cc.getRFControlVerificationList());
      Collections.sort(cc.getFacilityControlVerificationList());
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
        JPAUtil.initialize(cc.getBeamControlVerificationList());
        JPAUtil.initialize(cc.getRFControlVerificationList());
      }
    }

    return ccList;
  }

  @RolesAllowed("jam-admin")
  public void updateComments(BigInteger creditedControlId, String comments) {
    CreditedControl control = find(creditedControlId);

    control.setComments(comments);
  }

  @PermitAll
  public List<CreditedControl> filterList(Facility facility) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<CreditedControl> cq = cb.createQuery(CreditedControl.class);
    Root<CreditedControl> root = cq.from(CreditedControl.class);

    List<Order> orders = new ArrayList<>();
    List<Predicate> filters = new ArrayList<>();

    if (facility != null) {
      Join<CreditedControl, FacilityControlVerification> facilityControlVerificationJoin =
          root.join("facilityControlVerificationList");

      filters.add(
          cb.equal(
              facilityControlVerificationJoin.get("facilityControlVerificationPK").get("facility"),
              facility));

      Path p1 =
          facilityControlVerificationJoin.get("facilityControlVerificationPK").get("facility");
      Order o1 = cb.asc(p1);
      orders.add(o1);
    }

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }

    Path p0 = root.get("weight");
    Order o0 = cb.asc(p0);
    orders.add(o0);

    cq.orderBy(orders);

    cq.select(root);
    TypedQuery<CreditedControl> q = getEntityManager().createQuery(cq);

    return q.getResultList();
  }

  @PermitAll
  public List<CreditedControl> findWithFacilityVerification(Facility facility) {
    List<CreditedControl> ccList = filterList(facility);

    if (ccList != null) {
      for (CreditedControl cc : ccList) {
        Collections.sort(
            cc.getFacilityControlVerificationList(),
            new Comparator<FacilityControlVerification>() {
              @Override
              public int compare(FacilityControlVerification o1, FacilityControlVerification o2) {
                return o1.getFacilityControlVerificationPK()
                    .compareTo(o2.getFacilityControlVerificationPK());
              }
            });
      }
    }

    return ccList;
  }
}
