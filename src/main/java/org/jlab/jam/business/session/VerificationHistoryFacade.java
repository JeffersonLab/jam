package org.jlab.jam.business.session;

import java.math.BigInteger;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.jlab.jam.persistence.entity.BeamControlVerificationHistory;

/**
 * @author ryans
 */
@Stateless
public class VerificationHistoryFacade extends AbstractFacade<BeamControlVerificationHistory> {
  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public VerificationHistoryFacade() {
    super(BeamControlVerificationHistory.class);
  }

  @PermitAll
  public List<BeamControlVerificationHistory> findHistory(
      BigInteger controlVerificationId,
      int offset,
      int maxPerPage) { // join fetch a.controlVerification b join fetch b.creditedControl
    TypedQuery<BeamControlVerificationHistory> q =
        em.createQuery(
            "select a from BeamControlVerificationHistory a where a.beamControlVerification.beamControlVerificationId = :id order by a.beamControlVerificationHistoryId desc",
            BeamControlVerificationHistory.class);

    q.setParameter("id", controlVerificationId);

    return q.setFirstResult(offset).setMaxResults(maxPerPage).getResultList();
  }

  @PermitAll
  public Long countHistory(BigInteger controlVerificationId) {
    TypedQuery<Long> q =
        em.createQuery(
            "select count(a) from BeamControlVerificationHistory a where a.beamControlVerification.beamControlVerificationId = :id",
            Long.class);

    q.setParameter("id", controlVerificationId);

    return q.getSingleResult();
  }
}
