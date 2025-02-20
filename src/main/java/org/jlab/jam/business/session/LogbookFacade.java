package org.jlab.jam.business.session;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jlab.jam.persistence.entity.Facility;
import org.jlab.jam.persistence.entity.VerificationTeam;
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
public class LogbookFacade extends AbstractFacade<VerificationTeam> {

  private static final Logger LOGGER = Logger.getLogger(LogbookFacade.class.getName());

  @EJB RFAuthorizationFacade rfAuthorizationFacade;
  @EJB BeamAuthorizationFacade beamAuthorizationFacade;

  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public LogbookFacade() {
    super(VerificationTeam.class);
  }

  @PermitAll
  @Asynchronous
  public void sendAsyncAuthorizationLogEntry(
      Facility facility, OperationsType type, BigInteger authorizationId) {
    try {
      String proxyServer = System.getenv("FRONTEND_SERVER_URL");
      String logbookServer = System.getenv("LOGBOOK_SERVER_URL");

      long logId = sendAuthorizationLogEntry(facility, type, proxyServer, logbookServer);

      if (OperationsType.RF.equals(type)) {
        rfAuthorizationFacade.setLogEntry(authorizationId, logId, logbookServer);
      } else {
        beamAuthorizationFacade.setLogEntry(authorizationId, logId, logbookServer);
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error creating log entry", e);
    }
  }

  @RolesAllowed("jam-admin")
  public long sendELog(
      Facility facility, OperationsType type, String proxyServer, String logbookServer)
      throws UserFriendlyException {
    return sendAuthorizationLogEntry(facility, type, proxyServer, logbookServer);
  }

  @PermitAll
  public long sendAuthorizationLogEntry(
      Facility facility, OperationsType type, String proxyServer, String logbookServer)
      throws UserFriendlyException {
    String username = checkAuthenticated();

    // String body = getELogHTMLBody(authorization);
    String body = getAlternateELogHTMLBody(proxyServer);

    String subject = facility.getName() + " " + type.getLabel() + " Authorization Updated";

    String logbooks = facility.getLogbooksCsv();

    if (logbooks == null || logbooks.isEmpty()) {
      LOGGER.log(Level.WARNING, "No logbook_csv defined, skipping sendELog");
      throw new UserFriendlyException("No logbook_csv defined, skipping sendELog");
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
      tmpFile = grabPermissionsScreenshot(facility, type);
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

  private File grabPermissionsScreenshot(Facility facility, OperationsType type)
      throws IOException {

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
                + "%2Fjam%2Fauthorizations%2F"
                + facility.getPath().substring(1) // trim leading slash
                + "%3Ffocus%3D"
                + type
                + "%26print%3DY&fullPage=true&filename=jam.png&ignoreHTTPSErrors=true&waitUntil=networkidle2");

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
