package org.jlab.bam.business.session;

import java.math.BigInteger;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.jlab.bam.persistence.entity.VerificationHistory;

/**
 * @author ryans
 */
@Stateless
public class VerificationHistoryFacade extends AbstractFacade<VerificationHistory> {
  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public VerificationHistoryFacade() {
    super(VerificationHistory.class);
  }

  @PermitAll
  public List<VerificationHistory> findHistory(
      BigInteger controlVerificationId,
      int offset,
      int maxPerPage) { // join fetch a.controlVerification b join fetch b.creditedControl
    TypedQuery<VerificationHistory> q =
        em.createQuery(
            "select a from VerificationHistory a where a.controlVerification.controlVerificationId = :id order by a.verificationHistoryId desc",
            VerificationHistory.class);

    q.setParameter("id", controlVerificationId);

    return q.setFirstResult(offset).setMaxResults(maxPerPage).getResultList();
  }

  @PermitAll
  public Long countHistory(BigInteger controlVerificationId) {
    TypedQuery<Long> q =
        em.createQuery(
            "select count(a) from VerificationHistory a where a.controlVerification.controlVerificationId = :id",
            Long.class);

    q.setParameter("id", controlVerificationId);

    return q.getSingleResult();
  }
}
