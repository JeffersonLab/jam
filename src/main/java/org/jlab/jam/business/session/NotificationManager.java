package org.jlab.jam.business.session;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.jlab.jam.persistence.entity.*;
import org.jlab.jam.persistence.enumeration.OperationsType;
import org.jlab.jam.persistence.view.FacilityExpirationEvent;
import org.jlab.smoothness.business.util.IOUtil;

@Stateless
public class NotificationManager {
  private static final Logger LOGGER = Logger.getLogger(NotificationManager.class.getName());

  @EJB LogbookFacade logbookFacade;
  @EJB EmailFacade emailFacade;

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

    String typeStr = "";

    if (type != null) {
      typeStr = type.toString();
    }

    URL url =
        new URL(
            puppetServer
                + "/puppet-show/screenshot?url="
                + internalServer
                + "%2Fjam%2Fauthorizations%2F"
                + facility.getPath().substring(1) // trim leading slash
                + "%3Ffocus%3D"
                + typeStr
                + "%26print%3DY&fullPage=true&filename=jam.png&ignoreHTTPSErrors=true&waitUntil=networkidle2");

    File tmpFile;
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

  @Asynchronous
  public void asyncNotifyRFAuthorizerSave(RFAuthorization auth) {
    File screenshot = null;

    Facility facility = auth.getFacility();
    OperationsType type = OperationsType.RF;

    try {
      screenshot = grabPermissionsScreenshot(facility, type);
      logbookFacade.sendAuthorizationLogEntry(
          facility, type, auth.getRfAuthorizationId(), screenshot);
      emailFacade.sendAuthorizerChangeEmail(type, auth.getRfAuthorizationId(), screenshot);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to grab permissions screenshot.", e);
    } finally {
      if (screenshot != null) {
        boolean deleted = screenshot.delete();
        if (!deleted) {
          LOGGER.log(
              Level.WARNING,
              "Temporary image file was not deleted {0}",
              screenshot.getAbsolutePath());
        }
      }
    }
  }

  @Asynchronous
  public void asyncNotifyBeamAuthorizerSave(BeamAuthorization auth) {
    File screenshot = null;

    Facility facility = auth.getFacility();
    OperationsType type = OperationsType.BEAM;

    try {
      screenshot = grabPermissionsScreenshot(facility, type);
      logbookFacade.sendAuthorizationLogEntry(
          facility, type, auth.getBeamAuthorizationId(), screenshot);
      emailFacade.sendAuthorizerChangeEmail(type, auth.getBeamAuthorizationId(), screenshot);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to grab permissions screenshot.", e);
    } finally {
      if (screenshot != null) {
        boolean deleted = screenshot.delete();
        if (!deleted) {
          LOGGER.log(
              Level.WARNING,
              "Temporary image file was not deleted {0}",
              screenshot.getAbsolutePath());
        }
      }
    }
  }

  @Asynchronous
  public void asyncNotifyExpirationAndUpcoming(
      Map<Facility, FacilityExpirationEvent> facilityMap) {}

  @Asynchronous
  public void asyncNotifyFacilityExpiration(FacilityExpirationEvent event) {
    if (event != null && event.getExpirationCount() > 0) {
      System.err.println("Sending facility expiration notifications!");
      File screenshot = null;

      Facility facility = event.getFacility();
      OperationsType type = null;

      try {
        screenshot = grabPermissionsScreenshot(facility, type);
        logbookFacade.sendAuthorizationLogEntries(event, screenshot);
        emailFacade.sendAsyncExpirationEmails(event);
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Failed to grab permissions screenshot.", e);
      } finally {
        if (screenshot != null) {
          boolean deleted = screenshot.delete();
          if (!deleted) {
            LOGGER.log(
                Level.WARNING,
                "Temporary image file was not deleted {0}",
                screenshot.getAbsolutePath());
          }
        }
      }
    }
  }

  @Asynchronous
  public void asyncNotifyRFVerificationDowngrade(
      Facility facility, List<RFControlVerification> verificationList, RFAuthorization auth) {

    File screenshot = null;

    OperationsType type = OperationsType.RF;

    try {
      screenshot = grabPermissionsScreenshot(facility, type);
      logbookFacade.sendAuthorizationLogEntry(
          facility, type, auth.getRfAuthorizationId(), screenshot);

      emailFacade.sendAsyncRFVerifierDowngradeEmail(facility, verificationList);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to grab permissions screenshot.", e);
    } finally {
      if (screenshot != null) {
        boolean deleted = screenshot.delete();
        if (!deleted) {
          LOGGER.log(
              Level.WARNING,
              "Temporary image file was not deleted {0}",
              screenshot.getAbsolutePath());
        }
      }
    }
  }

  @Asynchronous
  public void asyncNotifyBeamVerificationDowngrade(
      Facility facility, List<BeamControlVerification> verificationList, BeamAuthorization auth) {
    File screenshot = null;

    OperationsType type = OperationsType.BEAM;

    try {
      screenshot = grabPermissionsScreenshot(facility, type);
      logbookFacade.sendAuthorizationLogEntry(
          facility, type, auth.getBeamAuthorizationId(), screenshot);

      emailFacade.sendAsyncBeamVerifierDowngradeEmail(facility, verificationList);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to grab permissions screenshot.", e);
    } finally {
      if (screenshot != null) {
        boolean deleted = screenshot.delete();
        if (!deleted) {
          LOGGER.log(
              Level.WARNING,
              "Temporary image file was not deleted {0}",
              screenshot.getAbsolutePath());
        }
      }
    }
  }
}
