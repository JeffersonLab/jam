package org.jlab.jam.business.session;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
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

  private File grabPermissionsScreenshot(Facility facility, OperationsType type, BigInteger authId)
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

    String path;
    String rfPath = "%2Frf-history%2Fsegments%3FrfAuthorizationId%3D" + authId.toString();
    String beamPath = "%2Fbeam-history%2Fdestinations%3FbeamAuthorizationId%3D" + authId.toString();

    if (OperationsType.RF.equals(type)) {
      path = rfPath;
    } else {
      path = beamPath;
    }

    URL url =
        new URL(
            puppetServer
                + "/puppet-show/screenshot?url="
                + internalServer
                + "%2Fjam%2Fauthorizations%2F"
                + facility.getPath().substring(1) // trim leading slash
                + path
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

  @PermitAll
  @Asynchronous
  public void asyncNotifyRFAuthorizerSave(RFAuthorization auth) {
    File screenshot = null;

    Facility facility = auth.getFacility();
    OperationsType type = OperationsType.RF;

    try {
      screenshot = grabPermissionsScreenshot(facility, type, auth.getRfAuthorizationId());
      logbookFacade.sendAuthorizationLogEntry(
          facility, type, auth.getRfAuthorizationId(), screenshot);
      emailFacade.sendWatcherAuthorizationUpdateEmail(facility, type, screenshot);
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

  @PermitAll
  @Asynchronous
  public void asyncNotifyBeamAuthorizerSave(BeamAuthorization auth) {
    File screenshot = null;

    Facility facility = auth.getFacility();
    OperationsType type = OperationsType.BEAM;

    try {
      screenshot = grabPermissionsScreenshot(facility, type, auth.getBeamAuthorizationId());
      logbookFacade.sendAuthorizationLogEntry(
          facility, type, auth.getBeamAuthorizationId(), screenshot);
      emailFacade.sendWatcherAuthorizationUpdateEmail(facility, type, screenshot);
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

  @PermitAll
  @Asynchronous
  public void asyncNotifyExpirationAndUpcoming(
      Map<Facility, FacilityExpirationEvent> facilityMap) {}

  @PermitAll
  @Asynchronous
  public void asyncNotifyFacilityExpiration(FacilityExpirationEvent event) {
    if (event != null && event.getExpirationCount() > 0) {
      File screenshot = null;

      Facility facility = event.getFacility();

      try {
        if (event.getRfEvent().getExpirationCount() > 0) {
          screenshot =
              grabPermissionsScreenshot(
                  facility,
                  OperationsType.RF,
                  event.getRfEvent().getAuthorization().getRfAuthorizationId());
          logbookFacade.sendAuthorizationLogEntry(
              event.getFacility(),
              OperationsType.RF,
              event.getRfEvent().getAuthorization().getRfAuthorizationId(),
              screenshot);

          emailFacade.sendRFExpirationEmails(event.getRfEvent(), screenshot);
        }

        if (event.getBeamEvent().getExpirationCount() > 0) {
          screenshot =
              grabPermissionsScreenshot(
                  facility,
                  OperationsType.BEAM,
                  event.getBeamEvent().getAuthorization().getBeamAuthorizationId());
          logbookFacade.sendAuthorizationLogEntry(
              event.getFacility(),
              OperationsType.BEAM,
              event.getBeamEvent().getAuthorization().getBeamAuthorizationId(),
              screenshot);

          emailFacade.sendBeamExpirationEmails(event.getBeamEvent(), screenshot);
        }
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

  @PermitAll
  @Asynchronous
  public void asyncNotifyRFVerificationDowngrade(
      Facility facility, List<RFControlVerification> verificationList, RFAuthorization auth) {

    File screenshot = null;

    OperationsType type = OperationsType.RF;

    try {
      screenshot = grabPermissionsScreenshot(facility, type, auth.getRfAuthorizationId());
      logbookFacade.sendAuthorizationLogEntry(
          facility, type, auth.getRfAuthorizationId(), screenshot);

      emailFacade.sendRFVerifierDowngradeEmail(auth, screenshot);
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

  @PermitAll
  @Asynchronous
  public void asyncNotifyBeamVerificationDowngrade(
      Facility facility, List<BeamControlVerification> verificationList, BeamAuthorization auth) {
    File screenshot = null;

    OperationsType type = OperationsType.BEAM;

    try {
      screenshot = grabPermissionsScreenshot(facility, type, auth.getBeamAuthorizationId());
      logbookFacade.sendAuthorizationLogEntry(
          facility, type, auth.getBeamAuthorizationId(), screenshot);

      emailFacade.sendBeamVerifierDowngradeEmail(auth, screenshot);
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
