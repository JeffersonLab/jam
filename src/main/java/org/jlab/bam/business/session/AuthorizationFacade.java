package org.jlab.bam.business.session;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import org.jlab.bam.persistence.entity.Authorization;
import org.jlab.bam.persistence.entity.BeamDestination;
import org.jlab.bam.persistence.entity.DestinationAuthorization;
import org.jlab.jlog.Body;
import org.jlab.jlog.Library;
import org.jlab.jlog.LogEntry;
import org.jlab.jlog.LogEntryAdminExtension;
import org.jlab.jlog.exception.AttachmentSizeException;
import org.jlab.jlog.exception.LogCertificateException;
import org.jlab.jlog.exception.LogIOException;
import org.jlab.jlog.exception.LogRuntimeException;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.service.EmailService;
import org.jlab.smoothness.business.util.IOUtil;

/**
 * @author ryans
 */
@Stateless
@DeclareRoles({"jam-admin"})
public class AuthorizationFacade extends AbstractFacade<Authorization> {

  private static final Logger LOGGER = Logger.getLogger(AuthorizationFacade.class.getName());

  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @EJB BeamDestinationFacade destinationFacade;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public AuthorizationFacade() {
    super(Authorization.class);
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
  public Authorization findCurrent() {
    Query q =
        em.createNativeQuery(
            "select * from (select * from authorization order by modified_date desc) where rownum <= 1",
            Authorization.class);

    List<Authorization> authorizationList = q.getResultList();

    Authorization authorization = null;

    if (authorizationList != null && !authorizationList.isEmpty()) {
      authorization = authorizationList.get(0);
    }

    return authorization;
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public List<Authorization> findHistory(int offset, int maxPerPage) {
    Query q =
        em.createNativeQuery(
            "select * from authorization order by authorization_date desc", Authorization.class);

    return q.setFirstResult(offset).setMaxResults(maxPerPage).getResultList();
  }

  @PermitAll
  public Long countHistory() {
    TypedQuery<Long> q = em.createQuery("select count(a) from Authorization a", Long.class);

    return q.getSingleResult();
  }

  @PermitAll
  public Map<BigInteger, DestinationAuthorization> createDestinationAuthorizationMap(
      Authorization authorization) {
    Map<BigInteger, DestinationAuthorization> destinationAuthorizationMap = new HashMap<>();

    if (authorization != null && authorization.getDestinationAuthorizationList() != null) {
      for (DestinationAuthorization destinationAuthorization :
          authorization.getDestinationAuthorizationList()) {
        destinationAuthorizationMap.put(
            destinationAuthorization.getDestinationAuthorizationPK().getBeamDestinationId(),
            destinationAuthorization);
      }
    }

    return destinationAuthorizationMap;
  }

  @RolesAllowed("jam-admin")
  public void saveAuthorization(
      String comments, List<DestinationAuthorization> destinationAuthorizationList)
      throws UserFriendlyException {
    String username = checkAuthenticated();

    Authorization authorization = new Authorization();
    authorization.setComments(comments);
    authorization.setAuthorizationDate(new Date());
    authorization.setAuthorizedBy(username);
    authorization.setModifiedDate(authorization.getAuthorizationDate());
    authorization.setModifiedBy(username);

    create(authorization);

    for (DestinationAuthorization da : destinationAuthorizationList) {

      BeamDestination destination =
          destinationFacade.find(da.getDestinationAuthorizationPK().getBeamDestinationId());
      if (!"None".equals(da.getBeamMode())) { // CW or Tune

        // Check if credited control agrees
        if (!(destination.getVerification().getVerificationId() <= 50)) {
          throw new UserFriendlyException(
              "Beam Destination \""
                  + destination.getName()
                  + "\" cannot have beam when credited controls are not verified");
        }

        // If provisional then there better be a comment
        if (destination.getVerification().getVerificationId() == 50
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

      da.setAuthorization(authorization);
      da.getDestinationAuthorizationPK().setAuthorizationId(authorization.getAuthorizationId());
      em.persist(da);
    }

    LOGGER.log(Level.FINE, "Director's Authorization saved successfully");
  }

  @RolesAllowed("jam-admin")
  public void sendOpsNewAuthorizationEmail(String linkHostName, String comments)
      throws UserFriendlyException {

    String toCsv = System.getenv("JAM_PERMISSIONS_EMAIL_CSV");

    if (toCsv == null || toCsv.isEmpty()) {
      LOGGER.log(
          Level.WARNING, "Environment variable 'JAM_PERMISSIONS_EMAIL_CSV' not found, aborting");
      return;
    }

    String subject = System.getenv("JAM_PERMISSIONS_SUBJECT");

    String body = "<a href=\"" + linkHostName + "/jam\">" + linkHostName + "/jam</a>";

    body = body + "\n\n<p>Notes: " + comments + "</p>";

    String sender = System.getenv("JAM_EMAIL_SENDER");

    if (sender == null || sender.isEmpty()) {
      LOGGER.log(Level.WARNING, "Environment variable 'JAM_EMAIL_SENDER' not found, aborting");
      return;
    }

    EmailService emailService = new EmailService();

    emailService.sendEmail(sender, sender, toCsv, null, subject, body, true);
  }

  @RolesAllowed("jam-admin")
  public long sendELog(String proxyServer, String logbookServer) throws UserFriendlyException {
    String username = checkAuthenticated();

    Authorization authorization = findCurrent();

    if (authorization == null) {
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
}
