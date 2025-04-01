package org.jlab.jam.business.session;

import java.math.BigInteger;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.jlab.jam.persistence.entity.RFControlVerificationHistory;

/**
 * @author ryans
 */
@Stateless
public class RFControlVerificationHistoryFacade
    extends AbstractFacade<RFControlVerificationHistory> {
  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public RFControlVerificationHistoryFacade() {
    super(RFControlVerificationHistory.class);
  }

  @PermitAll
  public List<RFControlVerificationHistory> findHistory(
      BigInteger controlVerificationId,
      int offset,
      int maxPerPage) { // join fetch a.controlVerification b join fetch b.creditedControl
    TypedQuery<RFControlVerificationHistory> q =
        em.createQuery(
            "select a from RFControlVerificationHistory a where a.rfControlVerification.rfControlVerificationId = :id order by a.rfControlVerificationHistoryId desc",
            RFControlVerificationHistory.class);

    q.setParameter("id", controlVerificationId);

    return q.setFirstResult(offset).setMaxResults(maxPerPage).getResultList();
  }

  @PermitAll
  public Long countHistory(BigInteger controlVerificationId) {
    TypedQuery<Long> q =
        em.createQuery(
            "select count(a) from RFControlVerificationHistory a where a.rfControlVerification.rfControlVerificationId = :id",
            Long.class);

    q.setParameter("id", controlVerificationId);

    return q.getSingleResult();
  }
}
