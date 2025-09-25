package org.jlab.jam.business.session;

import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.jam.business.util.EqualityHelper;
import org.jlab.jam.persistence.entity.*;
import org.jlab.jam.persistence.enumeration.OperationsType;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * @author ryans
 */
@Stateless
@DeclareRoles({"jam-admin"})
public class RFAuthorizationFacade extends AbstractFacade<RFAuthorization> {

  private static final Logger LOGGER = Logger.getLogger(RFAuthorizationFacade.class.getName());

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @EJB AuthorizerFacade authorizerFacade;
  @EJB RFSegmentFacade segmentFacade;
  @EJB NotificationManager notificationManager;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public RFAuthorizationFacade() {
    super(RFAuthorization.class);
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public HashMap<BigInteger, String> getUnitsMap() {
    HashMap<BigInteger, String> units = new HashMap<>();

    Query q =
        em.createNativeQuery(
            "select a.BEAM_DESTINATION_ID, a.CURRENT_LIMIT_UNITS from beam_destination a where a.ACTIVE_YN = 'Y'");

    List<Object[]> results = q.getResultList();

    for (Object[] result : results) {
      Object[] row = result;
      Number id = (Number) row[0];
      String unit = (String) row[1];
      // LOGGER.log(Level.WARNING, "ID: {0}, Unit: {1}", new Object[]{id, unit});
      units.put(BigInteger.valueOf(id.longValue()), unit);
    }

    return units;
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public RFAuthorization findCurrent(Facility facility) {
    Query q =
        em.createNativeQuery(
            "select * from (select * from rf_authorization where facility_id = :facility_id order by modified_date desc) where rownum <= 1",
            RFAuthorization.class);

    q.setParameter("facility_id", facility.getFacilityId());

    List<RFAuthorization> rfAuthorizationList = q.getResultList();

    RFAuthorization rfAuthorization = null;

    if (rfAuthorizationList != null && !rfAuthorizationList.isEmpty()) {
      rfAuthorization = rfAuthorizationList.get(0);
    }

    return rfAuthorization;
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public List<RFSegment> findHistory(Facility facility, int offset, int maxPerPage) {
    Query q =
        em.createNativeQuery(
            "select * from rf_authorization where facility_id = :facilityId order by modified_date desc",
            RFAuthorization.class);

    q.setParameter("facilityId", facility.getFacilityId());

    return q.setFirstResult(offset).setMaxResults(maxPerPage).getResultList();
  }

  @PermitAll
  public Long countHistory(Facility facility) {
    TypedQuery<Long> q =
        em.createQuery(
            "select count(a) from RFAuthorization a where a.facility.facilityId = :facilityId",
            Long.class);

    q.setParameter("facilityId", facility.getFacilityId());

    return q.getSingleResult();
  }

  @PermitAll
  public Map<BigInteger, RFSegmentAuthorization> createSegmentAuthorizationMap(
      RFAuthorization rfAuthorization) {
    Map<BigInteger, RFSegmentAuthorization> segmentAuthorizationMap = new HashMap<>();

    if (rfAuthorization != null && rfAuthorization.getRFSegmentAuthorizationList() != null) {
      for (RFSegmentAuthorization rfSegmentAuthorization :
          rfAuthorization.getRFSegmentAuthorizationList()) {
        segmentAuthorizationMap.put(
            rfSegmentAuthorization.getSegmentAuthorizationPK().getRFSegmentId(),
            rfSegmentAuthorization);
      }
    }

    return segmentAuthorizationMap;
  }

  @PermitAll
  public BigInteger saveAuthorization(
      Facility facility, String comments, List<RFSegmentAuthorization> segmentAuthorizationList)
      throws UserFriendlyException {
    String username = checkAuthenticated();

    if (!isAdmin()) {
      authorizerFacade.isAuthorizer(facility, OperationsType.RF, username);
    }

    RFAuthorization previousAuth = findCurrent(facility);

    RFAuthorization authorization = new RFAuthorization();
    authorization.setFacility(facility);
    authorization.setComments(comments);
    authorization.setAuthorizationDate(new Date());
    authorization.setAuthorizedBy(username);
    authorization.setModifiedDate(authorization.getAuthorizationDate());
    authorization.setModifiedBy(username);

    create(authorization);

    for (RFSegmentAuthorization da : segmentAuthorizationList) {

      RFSegment segment = segmentFacade.find(da.getSegmentAuthorizationPK().getRFSegmentId());
      if (da.isHighPowerRf()) {

        // Check if credited control agrees
        if (!(segment.getVerification().getVerificationStatusId() <= 50)) {
          throw new UserFriendlyException(
              "Segment \""
                  + segment.getName()
                  + "\" cannot authorize RF when credited controls are not verified");
        }

        // If provisional then there better be a comment
        if (segment.getVerification().getVerificationStatusId() == 50
            && (da.getComments() == null || da.getComments().trim().isEmpty())) {
          throw new UserFriendlyException(
              "Segment \""
                  + segment.getName()
                  + "\" must have a comment to explain why RF is permitted with provisional credited control status");
        }

        // Must provide an expiration date since ON
        if (da.getExpirationDate() == null) {
          throw new UserFriendlyException(
              "Segment \""
                  + segment.getName()
                  + "\" must have an expiration date since RF is allowed");
        }

        // Expiration must be in the future
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        if (da.getExpirationDate().before(cal.getTime())) {
          throw new UserFriendlyException(
              "Segment \""
                  + segment.getName()
                  + "\" must have a future expiration date and minimum expiration is 1 hour from now");
        }
      } else { // High Power = OFF
        // We force expiration to empty
        da.setExpirationDate(null);
        da.setComments(null);
      }

      da.setRFAuthorization(authorization);
      da.getSegmentAuthorizationPK().setRFAuthorizationId(authorization.getRfAuthorizationId());

      boolean changed = isChanged(da, previousAuth);

      da.setChanged(changed);

      em.persist(da);
    }

    LOGGER.log(Level.FINE, "Director's Authorization saved successfully");

    notificationManager.asyncNotifyRFAuthorizerSave(authorization);

    return authorization.getRfAuthorizationId();
  }

  private boolean isChanged(RFSegmentAuthorization newSegAuth, RFAuthorization previousAuth) {
    boolean changed = true;

    // System.err.println("RF Authorization changed?: " + newSegAuth);

    if (previousAuth != null) {
      List<RFSegmentAuthorization> segList = previousAuth.getRFSegmentAuthorizationList();

      // System.err.println("segList: " + segList == null ? "null" : segList.size());

      for (RFSegmentAuthorization oldSegAuth : segList) {
        if (oldSegAuth
            .getSegmentAuthorizationPK()
            .getRFSegmentId()
            .equals(newSegAuth.getSegmentAuthorizationPK().getRFSegmentId())) {
          /*System.err.println("Found matching segment \"" + oldSegAuth.getSegment() + "\"");
          System.err.println(
              "RF Change \"" + (oldSegAuth.isHighPowerRf() != newSegAuth.isHighPowerRf()) + "\"");
          System.err.println(
              "Date Change \""
                  + !EqualityHelper.nullableDateEqual(
                      oldSegAuth.getExpirationDate(), newSegAuth.getExpirationDate())
                  + "\" - \""
                  + oldSegAuth.getExpirationDate()
                  + "\" vs \""
                  + newSegAuth.getExpirationDate()
                  + "\"");
          System.err.println(
              "Comment Change \""
                  + !EqualityHelper.nullableStringEqual(
                      oldSegAuth.getComments(), newSegAuth.getComments())
                  + "\"");*/

          // Check if change
          changed =
              (oldSegAuth.isHighPowerRf() != newSegAuth.isHighPowerRf())
                  || !EqualityHelper.nullableDateEqual(
                      oldSegAuth.getExpirationDate(), newSegAuth.getExpirationDate())
                  || !EqualityHelper.nullableStringEqual(
                      oldSegAuth.getComments(), newSegAuth.getComments());

          break;
        }
      }
    }

    return changed;
  }

  @PermitAll
  public void setLogEntry(BigInteger rfAuthorizationId, Long logId, String logbookServer) {
    RFAuthorization current = find(rfAuthorizationId);

    if (current != null && logId != null) {
      String url = logbookServer + "/entry/" + logId;

      current.setLogentryUrl(url);
    }
  }
}
