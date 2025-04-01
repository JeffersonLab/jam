package org.jlab.jam.business.session;

import java.io.*;
import java.math.BigInteger;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
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

/**
 * @author ryans
 */
@Stateless
public class LogbookFacade extends AbstractFacade<VerificationTeam> {

  private static final Logger LOGGER = Logger.getLogger(LogbookFacade.class.getName());

  @EJB RFAuthorizationFacade rfAuthorizationFacade;
  @EJB BeamAuthorizationFacade beamAuthorizationFacade;

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public LogbookFacade() {
    super(VerificationTeam.class);
  }

  @PermitAll
  public void sendAuthorizationLogEntry(
      String username,
      Facility facility,
      OperationsType type,
      BigInteger authorizationId,
      File screenshot) {
    try {
      String proxyServer = System.getenv("FRONTEND_SERVER_URL");
      String logbookServer = System.getenv("LOGBOOK_SERVER_URL");

      long logId =
          sendAuthorizationLogEntry(
              username, facility, type, proxyServer, logbookServer, screenshot);

      if (OperationsType.RF.equals(type)) {
        rfAuthorizationFacade.setLogEntry(authorizationId, logId, logbookServer);
      } else {
        beamAuthorizationFacade.setLogEntry(authorizationId, logId, logbookServer);
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error creating log entry", e);
    }
  }

  private long sendAuthorizationLogEntry(
      String username,
      Facility facility,
      OperationsType type,
      String proxyServer,
      String logbookServer,
      File screenshot)
      throws UserFriendlyException {

    final String DEFAULT_USERNAME = "jamgr";

    if (username == null) {
      username = DEFAULT_USERNAME;
    }

    // String body = getELogHTMLBody(authorization);
    String body = getAlternateELogHTMLBody(proxyServer);

    String subject =
        "JAM: " + facility.getName() + " " + type.getLabel() + " Authorization Updated";

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

    try {
      entry.addAttachment(screenshot.getAbsolutePath());
      logId = entry.submitNow();

    } catch (AttachmentSizeException
        | LogIOException
        | LogRuntimeException
        | LogCertificateException e) {
      throw new UserFriendlyException("Unable to send elog", e);
    }

    return logId;
  }

  private String getAlternateELogHTMLBody(String server) {
    StringBuilder builder = new StringBuilder();

    builder.append(
        "[figure:1]<div>\n\n<b><span style=\"color: red;\">Always check the Authorization web application for the latest status:</span></b> ");
    builder.append("<a href=\"");
    builder.append(server);
    builder.append("/jam/\">JLab Authorization Manager</a></div>\n");

    return builder.toString();
  }
}
