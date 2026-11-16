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
import java.util.List;
import org.jlab.jam.persistence.entity.Facility;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * @author ryans
 */
@Stateless
public class FacilityFacade extends AbstractFacade<Facility> {
  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public FacilityFacade() {
    super(Facility.class);
  }

  @PermitAll
  public Facility findByPath(String pathInfo) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Facility> cq = cb.createQuery(Facility.class);
    Root<Facility> root = cq.from(Facility.class);

    List<Predicate> filters = new ArrayList<>();

    filters.add(cb.equal(root.get("path"), pathInfo));

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }

    cq.select(root);
    TypedQuery<Facility> q = getEntityManager().createQuery(cq).setFirstResult(0).setMaxResults(2);
    List<Facility> recordList = q.getResultList();

    Facility facility = null;

    if (recordList.size() > 1) {
      throw new RuntimeException("Duplicate facility paths configured");
    }

    if (recordList.size() > 0) {
      facility = recordList.get(0);
    }

    return facility;
  }

  @RolesAllowed("jam-admin")
  public void editFacility(BigInteger facilityId, String managerUsername, String logbooksCsv)
      throws UserFriendlyException {
    if (facilityId == null) {
      throw new UserFriendlyException("facility ID is required");
    }

    if (managerUsername != null) {
      managerUsername = managerUsername.trim();
    }

    if (managerUsername == null || managerUsername.isEmpty()) {
      throw new UserFriendlyException("manager is required");
    }

    if (managerUsername.length() > 32) {
      throw new UserFriendlyException("manager username must be 32 characters or fewer");
    }

    logbooksCsv = normalizeCsv(logbooksCsv);

    if (logbooksCsv != null && logbooksCsv.length() > 64) {
      throw new UserFriendlyException("logbooks must be 64 characters or fewer");
    }

    Facility facility = find(facilityId);

    if (facility == null) {
      throw new UserFriendlyException("facility not found with id " + facilityId);
    }

    facility.setManagerUsername(managerUsername);
    facility.setLogbooksCsv(logbooksCsv);

    edit(facility);
  }

  /**
   * Trims each comma-separated value and drops empty ones.
   *
   * @param csv The comma-separated values, such as "TLOG, ELOG"
   * @return The values joined with commas, such as "TLOG,ELOG", or null if there are none
   */
  static String normalizeCsv(String csv) {
    if (csv == null) {
      return null;
    }

    List<String> values = new ArrayList<>();

    for (String value : csv.split(",")) {
      value = value.trim();

      if (!value.isEmpty()) {
        values.add(value);
      }
    }

    return values.isEmpty() ? null : String.join(",", values);
  }
}
