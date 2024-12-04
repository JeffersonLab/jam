package org.jlab.jam.business.session;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.jlab.jam.persistence.entity.BeamDestination;
import org.jlab.jam.persistence.entity.ControlVerification;

/**
 * @author ryans
 */
@Stateless
public class BeamDestinationFacade extends AbstractFacade<BeamDestination> {
  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public BeamDestinationFacade() {
    super(BeamDestination.class);
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public List<BeamDestination> findActiveDestinations() {
    Query q =
        em.createNativeQuery(
            "select * from JAM_OWNER.beam_destination where ACTIVE_YN = 'Y' order by weight",
            BeamDestination.class);

    return q.getResultList();
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public List<BeamDestination> findCebafDestinations() {
    Query q =
        em.createNativeQuery(
            "select * from JAM_OWNER.beam_destination where machine = 'CEBAF' and ACTIVE_YN = 'Y' order by weight",
            BeamDestination.class);

    return q.getResultList();
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public List<BeamDestination> findLerfDestinations() {
    Query q =
        em.createNativeQuery(
            "select * from JAM_OWNER.beam_destination where machine = 'LERF'  and ACTIVE_YN = 'Y' order by weight",
            BeamDestination.class);

    return q.getResultList();
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public List<BeamDestination> findUitfDestinations() {
    Query q =
        em.createNativeQuery(
            "select * from JAM_OWNER.beam_destination where machine = 'UITF'  and ACTIVE_YN = 'Y' order by weight",
            BeamDestination.class);

    return q.getResultList();
  }

  @PermitAll
  public BeamDestination findWithVerificationList(BigInteger destinationId) {
    TypedQuery<BeamDestination> q =
        em.createQuery(
            "select a from BeamDestination a where a.beamDestinationId = :destinationId",
            BeamDestination.class);

    q.setParameter("destinationId", destinationId);

    List<BeamDestination> destinationList = q.getResultList();

    BeamDestination destination = null;

    if (destinationList != null && !destinationList.isEmpty()) {
      destination = destinationList.get(0);

      // JPAUtil.initialize(destination.getControlVerificationList());
      for (ControlVerification verification : destination.getControlVerificationList()) {
        verification.getCreditedControl().getName();
      }

      Collections.sort(
          destination.getControlVerificationList(),
          new Comparator<ControlVerification>() {
            @Override
            public int compare(ControlVerification o1, ControlVerification o2) {
              return o1.getCreditedControl().compareTo(o2.getCreditedControl());
            }
          });
    }

    return destination;
  }
}
