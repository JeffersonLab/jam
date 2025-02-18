package org.jlab.jam.business.session;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.jlab.jam.persistence.entity.BeamAuthorization;
import org.jlab.jam.persistence.entity.BeamDestination;
import org.jlab.jam.persistence.entity.BeamDestinationAuthorization;
import org.jlab.jam.persistence.entity.Facility;
import org.jlab.jam.persistence.enumeration.OperationsType;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * @author ryans
 */
@Stateless
@DeclareRoles({"jam-admin"})
public class BeamAuthorizationFacade extends AbstractFacade<BeamAuthorization> {

  private static final Logger LOGGER = Logger.getLogger(BeamAuthorizationFacade.class.getName());

  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @EJB AuthorizerFacade authorizerFacade;
  @EJB BeamDestinationFacade destinationFacade;

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
  public BeamAuthorization findCurrent() {
    Query q =
        em.createNativeQuery(
            "select * from (select * from beam_authorization order by modified_date desc) where rownum <= 1",
            BeamAuthorization.class);

    List<BeamAuthorization> beamAuthorizationList = q.getResultList();

    BeamAuthorization beamAuthorization = null;

    if (beamAuthorizationList != null && !beamAuthorizationList.isEmpty()) {
      beamAuthorization = beamAuthorizationList.get(0);
    }

    return beamAuthorization;
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public List<BeamAuthorization> findHistory(int offset, int maxPerPage) {
    Query q =
        em.createNativeQuery(
            "select * from beam_authorization order by authorization_date desc",
            BeamAuthorization.class);

    return q.setFirstResult(offset).setMaxResults(maxPerPage).getResultList();
  }

  @PermitAll
  public Long countHistory() {
    TypedQuery<Long> q = em.createQuery("select count(a) from BeamAuthorization a", Long.class);

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
  public void saveAuthorization(
      Facility facility,
      String comments,
      List<BeamDestinationAuthorization> beamDestinationAuthorizationList)
      throws UserFriendlyException {
    String username = checkAuthenticated();

    if (!isAdmin()) {
      authorizerFacade.isAuthorizer(facility, OperationsType.BEAM, username);
    }

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
      }

      da.setAuthorization(beamAuthorization);
      da.getDestinationAuthorizationPK()
          .setAuthorizationId(beamAuthorization.getBeamAuthorizationId());
      em.persist(da);
    }

    LOGGER.log(Level.FINE, "Director's Authorization saved successfully");
  }

  @PermitAll
  public void setLogEntry(Long logId, String logbookServer) {
    BeamAuthorization current = findCurrent();

    if (current != null && logId != null) {
      String url = logbookServer + "/entry/" + logId;

      current.setLogentryUrl(url);
    }
  }
}
