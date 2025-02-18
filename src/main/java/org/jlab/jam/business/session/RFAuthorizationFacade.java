package org.jlab.jam.business.session;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.jlab.jam.persistence.entity.*;
import org.jlab.jam.persistence.enumeration.OperationsType;
import org.jlab.jlog.Body;
import org.jlab.jlog.Library;
import org.jlab.jlog.LogEntry;
import org.jlab.jlog.LogEntryAdminExtension;
import org.jlab.jlog.exception.AttachmentSizeException;
import org.jlab.jlog.exception.LogCertificateException;
import org.jlab.jlog.exception.LogIOException;
import org.jlab.jlog.exception.LogRuntimeException;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.IOUtil;

/**
 * @author ryans
 */
@Stateless
@DeclareRoles({"jam-admin"})
public class RFAuthorizationFacade extends AbstractFacade<RFAuthorization> {

  private static final Logger LOGGER = Logger.getLogger(RFAuthorizationFacade.class.getName());

  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @EJB AuthorizerFacade authorizerFacade;
  @EJB RFSegmentFacade segmentFacade;

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
  public RFAuthorization findCurrent() {
    Query q =
        em.createNativeQuery(
            "select * from (select * from rf_authorization order by modified_date desc) where rownum <= 1",
            RFAuthorization.class);

    List<RFAuthorization> rfAuthorizationList = q.getResultList();

    RFAuthorization rfAuthorization = null;

    if (rfAuthorizationList != null && !rfAuthorizationList.isEmpty()) {
      rfAuthorization = rfAuthorizationList.get(0);
    }

    return rfAuthorization;
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public List<RFSegment> findHistory(int offset, int maxPerPage) {
    Query q =
        em.createNativeQuery(
            "select * from rf_authorization order by authorization_date desc",
            RFAuthorization.class);

    return q.setFirstResult(offset).setMaxResults(maxPerPage).getResultList();
  }

  @PermitAll
  public Long countHistory() {
    TypedQuery<Long> q = em.createQuery("select count(a) from RFAuthorization a", Long.class);

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
  public void saveAuthorization(
      Facility facility, String comments, List<RFSegmentAuthorization> segmentAuthorizationList)
      throws UserFriendlyException {
    String username = checkAuthenticated();

    if(!isAdmin()) {
      authorizerFacade.isAuthorizer(facility, OperationsType.RF, username);
    }

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
      }

      da.setRFAuthorization(authorization);
      da.getSegmentAuthorizationPK().setRFAuthorizationId(authorization.getRfAuthorizationId());
      em.persist(da);
    }

    LOGGER.log(Level.FINE, "Director's Authorization saved successfully");
  }

  @RolesAllowed("jam-admin")
  public long sendELog(String proxyServer, String logbookServer) throws UserFriendlyException {
    String username = checkAuthenticated();

    RFAuthorization rfAuthorization = findCurrent();

    if (rfAuthorization == null) {
      throw new UserFriendlyException("No authorizations found");
    }

    // String body = getELogHTMLBody(authorization);
    String body = getAlternateELogHTMLBody(proxyServer);

    String subject = System.getenv("JAM_PERMISSIONS_SUBJECT");

    String logbooks = System.getenv("JAM_BOOKS_CSV");

    if (logbooks == null || logbooks.isEmpty()) {
      logbooks = "TLOG";
      LOGGER.log(
          Level.WARNING, "Environment variable 'JAM_BOOKS_CSV' not found, using default TLOG");
    }

    Properties config = Library.getConfiguration();

    config.setProperty("SUBMIT_URL", logbookServer + "/incoming");
    config.setProperty("FETCH_URL", logbookServer + "/entry");

    LogEntry entry = new LogEntry(subject, logbooks);

    entry.setBody(body, Body.ContentType.HTML);
    entry.setTags("Readme");

    LogEntryAdminExtension extension = new LogEntryAdminExtension(entry);
    extension.setAuthor(username);

    long logId;

    // System.out.println(entry.getXML());
    File tmpFile = null;

    try {
      tmpFile = grabPermissionsScreenshot();
      entry.addAttachment(tmpFile.getAbsolutePath());
      logId = entry.submitNow();

    } catch (IOException
        | AttachmentSizeException
        | LogIOException
        | LogRuntimeException
        | LogCertificateException e) {
      throw new UserFriendlyException("Unable to send elog", e);
    } finally {
      if (tmpFile != null) {
        boolean deleted = tmpFile.delete();
        if (!deleted) {
          LOGGER.log(
              Level.WARNING, "Temporary image file was not deleted {0}", tmpFile.getAbsolutePath());
        }
      }
    }

    return logId;
  }

  private File grabPermissionsScreenshot() throws IOException {

    String puppetServer = System.getenv("PUPPET_SHOW_SERVER_URL");
    String internalServer = System.getenv("BACKEND_SERVER_URL");

    if (puppetServer == null) {
      puppetServer = "http://localhost";
    }

    if (internalServer == null) {
      internalServer = "http://localhost";
    }

    internalServer = URLEncoder.encode(internalServer, StandardCharsets.UTF_8);

    URL url =
        new URL(
            puppetServer
                + "/puppet-show/screenshot?url="
                + internalServer
                + "%2Fjam%2Fpermissions%3Fprint%3DY&fullPage=true&filename=jam.png&ignoreHTTPSErrors=true");

    LOGGER.log(Level.FINEST, "Fetching URL: {0}", url.toString());

    File tmpFile = null;
    InputStream in = null;
    OutputStream out = null;

    try {
      URLConnection con = url.openConnection();
      in = con.getInputStream();

      tmpFile = File.createTempFile("jam", ".png");
      out = new FileOutputStream(tmpFile);
      IOUtil.copy(in, out);

    } finally {
      IOUtil.close(in, out);
    }
    return tmpFile;
  }

  private String getAlternateELogHTMLBody(String server) {
    StringBuilder builder = new StringBuilder();

    builder.append(
        "[figure:1]<div>\n\n<b><span style=\"color: red;\">Always check the Beam Authorization web application for the latest credited controls status:</span></b> ");
    builder.append("<a href=\"");
    builder.append(server);
    builder.append("/jam/\">JLab Authorization Manager</a></div>\n");

    return builder.toString();
  }

  @PermitAll
  public void setLogEntry(Long logId, String logbookServer) {
    RFAuthorization current = findCurrent();

    if (current != null && logId != null) {
      String url = logbookServer + "/entry/" + logId;

      current.setLogentryUrl(url);
    }
  }
}
