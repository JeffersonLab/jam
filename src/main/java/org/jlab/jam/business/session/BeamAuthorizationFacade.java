package org.jlab.jam.business.session;

import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class BeamAuthorizationFacade extends AbstractFacade<BeamAuthorization> {

  private static final Logger LOGGER = Logger.getLogger(BeamAuthorizationFacade.class.getName());

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @EJB AuthorizerFacade authorizerFacade;
  @EJB BeamDestinationFacade destinationFacade;
  @EJB NotificationManager notificationManager;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public BeamAuthorizationFacade() {
    super(BeamAuthorization.class);
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
  public BeamAuthorization findCurrent(Facility facility) {
    Query q =
        em.createNativeQuery(
            "select * from (select * from beam_authorization where facility_id = :facility_id order by modified_date desc) where rownum <= 1",
            BeamAuthorization.class);

    q.setParameter("facility_id", facility.getFacilityId());

    List<BeamAuthorization> beamAuthorizationList = q.getResultList();

    BeamAuthorization beamAuthorization = null;

    if (beamAuthorizationList != null && !beamAuthorizationList.isEmpty()) {
      beamAuthorization = beamAuthorizationList.get(0);
    }

    return beamAuthorization;
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public List<BeamAuthorization> findHistory(Facility facility, int offset, int maxPerPage) {
    Query q =
        em.createNativeQuery(
            "select * from beam_authorization where facility_id = :facilityId order by modified_date desc",
            BeamAuthorization.class);

    q.setParameter("facilityId", facility.getFacilityId());

    return q.setFirstResult(offset).setMaxResults(maxPerPage).getResultList();
  }

  @PermitAll
  public Long countHistory(Facility facility) {
    TypedQuery<Long> q =
        em.createQuery(
            "select count(a) from BeamAuthorization a where a.facility.facilityId = :facilityId",
            Long.class);

    q.setParameter("facilityId", facility.getFacilityId());

    return q.getSingleResult();
  }

  @PermitAll
  public Map<BigInteger, BeamDestinationAuthorization> createDestinationAuthorizationMap(
      BeamAuthorization beamAuthorization) {
    Map<BigInteger, BeamDestinationAuthorization> destinationAuthorizationMap = new HashMap<>();

    if (beamAuthorization != null && beamAuthorization.getDestinationAuthorizationList() != null) {
      for (BeamDestinationAuthorization beamDestinationAuthorization :
          beamAuthorization.getDestinationAuthorizationList()) {
        destinationAuthorizationMap.put(
            beamDestinationAuthorization.getDestinationAuthorizationPK().getBeamDestinationId(),
            beamDestinationAuthorization);
      }
    }

    return destinationAuthorizationMap;
  }

  @PermitAll
  public BigInteger saveAuthorization(
      Facility facility,
      String comments,
      List<BeamDestinationAuthorization> beamDestinationAuthorizationList)
      throws UserFriendlyException {
    String username = checkAuthenticated();

    if (!isAdmin()) {
      authorizerFacade.isAuthorizer(facility, OperationsType.BEAM, username);
    }

    BeamAuthorization previousAuth = findCurrent(facility);

    BeamAuthorization beamAuthorization = new BeamAuthorization();
    beamAuthorization.setFacility(facility);
    beamAuthorization.setComments(comments);
    beamAuthorization.setAuthorizationDate(new Date());
    beamAuthorization.setAuthorizedBy(username);
    beamAuthorization.setModifiedDate(beamAuthorization.getAuthorizationDate());
    beamAuthorization.setModifiedBy(username);

    create(beamAuthorization);

    for (BeamDestinationAuthorization da : beamDestinationAuthorizationList) {

      BeamDestination destination =
          destinationFacade.find(da.getDestinationAuthorizationPK().getBeamDestinationId());
      if (!"None".equals(da.getBeamMode())) { // CW or Tune

        // Check if credited control agrees
        if (!(destination.getVerification().getVerificationStatusId() <= 50)) {
          throw new UserFriendlyException(
              "Beam Destination \""
                  + destination.getName()
                  + "\" cannot have beam when credited controls are not verified");
        }

        // If provisional then there better be a comment
        if (destination.getVerification().getVerificationStatusId() == 50
            && (da.getComments() == null || da.getComments().trim().isEmpty())) {
          throw new UserFriendlyException(
              "Beam Destination \""
                  + destination.getName()
                  + "\" must have a comment to explain why beam is permitted with provisional credited control status");
        }

        // Must provide an expiration date since CW or Tune
        if (da.getExpirationDate() == null) {
          throw new UserFriendlyException(
              "Beam Destination \""
                  + destination.getName()
                  + "\" must have an expiration date since beam is allowed");
        }

        // Expiration must be in the future
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        if (da.getExpirationDate().before(cal.getTime())) {
          throw new UserFriendlyException(
              "Beam Destination \""
                  + destination.getName()
                  + "\" must have a future expiration date and minimum expiration is 1 hour from now");
        }
      } else { // mode = NONE (OFF)
        // We force expiration to empty
        da.setExpirationDate(null);
        da.setCwLimit(null);
        da.setComments(null);
      }

      da.setAuthorization(beamAuthorization);
      da.getDestinationAuthorizationPK()
          .setAuthorizationId(beamAuthorization.getBeamAuthorizationId());

      boolean changed = isChanged(da, previousAuth);

      da.setChanged(changed);

      em.persist(da);
    }

    LOGGER.log(Level.FINE, "Director's Authorization saved successfully");

    notificationManager.asyncNotifyBeamAuthorizerSave(beamAuthorization);

    return beamAuthorization.getBeamAuthorizationId();
  }

  private boolean isChanged(
      BeamDestinationAuthorization newDestAuth, BeamAuthorization previousAuth) {
    boolean changed = true;

    // System.err.println("Beam Authorization changed?: " + newDestAuth);

    if (previousAuth != null) {
      List<BeamDestinationAuthorization> destList = previousAuth.getDestinationAuthorizationList();

      // System.err.println("destList: " + destList == null ? "null" : destList.size());

      for (BeamDestinationAuthorization oldDestAuth : destList) {
        if (oldDestAuth
            .getDestinationAuthorizationPK()
            .getBeamDestinationId()
            .equals(newDestAuth.getDestinationAuthorizationPK().getBeamDestinationId())) {
          /*System.err.println("Found matching destination \"" + oldDestAuth.getDestination() + "\"");
          System.err.println(
              "Beam Mode Change \""
                  + !EqualityHelper.nullableStringEqual(
                      oldDestAuth.getBeamMode(), newDestAuth.getBeamMode())
                  + "\"");
          System.err.println(
              "Current Limit Change \""
                  + !EqualityHelper.nullableObjEqual(
                      oldDestAuth.getCwLimit(), newDestAuth.getCwLimit())
                  + "\"");
          System.err.println(
              "Date Change \""
                  + !EqualityHelper.nullableDateEqual(
                      oldDestAuth.getExpirationDate(), newDestAuth.getExpirationDate())
                  + "\" - \""
                  + oldDestAuth.getExpirationDate()
                  + "\" vs \""
                  + newDestAuth.getExpirationDate()
                  + "\"");
          System.err.println(
              "Comment Change \""
                  + !EqualityHelper.nullableStringEqual(
                      oldDestAuth.getComments(), newDestAuth.getComments())
                  + "\"");*/

          // Check if change
          changed =
              (!EqualityHelper.nullableStringEqual(
                      oldDestAuth.getBeamMode(), newDestAuth.getBeamMode()))
                  || (!EqualityHelper.nullableObjEqual(
                      oldDestAuth.getCwLimit(), newDestAuth.getCwLimit()))
                  || !EqualityHelper.nullableDateEqual(
                      oldDestAuth.getExpirationDate(), newDestAuth.getExpirationDate())
                  || !EqualityHelper.nullableStringEqual(
                      oldDestAuth.getComments(), newDestAuth.getComments());

          break;
        }
      }
    }

    return changed;
  }

  @PermitAll
  public void setLogEntry(BigInteger beamAuthorizationId, Long logId, String logbookServer) {
    BeamAuthorization current = find(beamAuthorizationId);

    if (current != null && logId != null) {
      String url = logbookServer + "/entry/" + logId;

      current.setLogentryUrl(url);
    }
  }
}
