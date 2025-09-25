package org.jlab.jam.business.session;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.jlab.jam.persistence.entity.CreditedControl;
import org.jlab.jam.persistence.entity.Facility;
import org.jlab.jam.persistence.entity.VerificationTeam;
import org.jlab.jam.persistence.view.FacilityControlVerification;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.persistence.util.JPAUtil;

/**
 * @author ryans
 */
@Stateless
public class CreditedControlFacade extends AbstractFacade<CreditedControl> {

  @PersistenceContext(unitName = "webappPU")
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

  @PermitAll
  public List<CreditedControl> filterList(Facility facility, VerificationTeam team) {
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

    if (team != null) {
      filters.add(cb.equal(root.get("verificationTeam"), team));
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
  public List<CreditedControl> findWithFacilityVerification(
      Facility facility, VerificationTeam team) {
    List<CreditedControl> ccList = filterList(facility, team);

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

        if (facility != null) {
          for (FacilityControlVerification facilityControlVerification :
              new ArrayList<>(cc.getFacilityControlVerificationList())) {
            if (!facilityControlVerification
                .getFacilityControlVerificationPK()
                .getFacility()
                .equals(facility)) {
              cc.getFacilityControlVerificationList().remove(facilityControlVerification);
            }
          }
        }
      }
    }

    return ccList;
  }

  @RolesAllowed("jam-admin")
  public void addControl(
      String name, String description, String doc, BigInteger teamId, String frequency)
      throws UserFriendlyException {
    if (name == null || name.isEmpty()) {
      throw new UserFriendlyException("name is required");
    }

    if (description == null || description.isEmpty()) {
      throw new UserFriendlyException("description is required");
    }

    if (teamId == null) {
      throw new UserFriendlyException("team is required");
    }

    VerificationTeam team = em.find(VerificationTeam.class, teamId);

    if (team == null) {
      throw new UserFriendlyException("team not found with id " + teamId);
    }

    if (frequency == null || frequency.isEmpty()) {
      throw new UserFriendlyException("frequency is required");
    }

    CreditedControl cc = new CreditedControl();

    cc.setName(name);
    cc.setDescription(description);
    cc.setDocLabelUrlCsv(doc);
    cc.setVerificationTeam(team);
    cc.setVerificationFrequency(frequency);

    create(cc);
  }

  @RolesAllowed("jam-admin")
  public void removeControl(BigInteger controlId) throws UserFriendlyException {
    if (controlId == null) {
      throw new UserFriendlyException("control ID is required");
    }

    CreditedControl control = find(controlId);

    if (control == null) {
      throw new UserFriendlyException("control not found with id " + controlId);
    }

    remove(control);
  }

  @RolesAllowed("jam-admin")
  public void editControl(
      BigInteger controlId,
      String name,
      String description,
      String doc,
      BigInteger teamId,
      String frequency)
      throws UserFriendlyException {
    if (name == null || name.isEmpty()) {
      throw new UserFriendlyException("name is required");
    }

    if (description == null || description.isEmpty()) {
      throw new UserFriendlyException("description is required");
    }

    if (teamId == null) {
      throw new UserFriendlyException("team is required");
    }

    VerificationTeam team = em.find(VerificationTeam.class, teamId);

    if (team == null) {
      throw new UserFriendlyException("team not found with id " + teamId);
    }

    if (frequency == null || frequency.isEmpty()) {
      throw new UserFriendlyException("frequency is required");
    }

    if (controlId == null) {
      throw new UserFriendlyException("control ID is required");
    }

    CreditedControl cc = find(controlId);

    if (cc == null) {
      throw new UserFriendlyException("control not found with id " + controlId);
    }

    cc.setName(name);
    cc.setDescription(description);
    cc.setDocLabelUrlCsv(doc);
    cc.setVerificationTeam(team);
    cc.setVerificationFrequency(frequency);

    edit(cc);
  }
}
