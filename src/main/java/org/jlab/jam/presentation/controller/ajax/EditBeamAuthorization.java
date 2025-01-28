package org.jlab.jam.presentation.controller.ajax;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.jam.business.session.BeamAuthorizationFacade;
import org.jlab.jam.business.session.FacilityFacade;
import org.jlab.jam.persistence.entity.BeamDestinationAuthorization;
import org.jlab.jam.persistence.entity.DestinationAuthorizationPK;
import org.jlab.jam.persistence.entity.Facility;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "EditBeamAuthorization",
    urlPatterns = {"/ajax/edit-beam-auth"})
public class EditBeamAuthorization extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(EditBeamAuthorization.class.getName());

  @EJB BeamAuthorizationFacade beamAuthorizationFacade;
  @EJB FacilityFacade facilityFacade;

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String errorReason = null;
    Long logId = null;
    Facility facility = null;
    String comments = null;
    Boolean sendNotifications = false;

    try {
      BigInteger facilityId = ParamConverter.convertBigInteger(request, "facilityId");

      if (facilityId == null) {
        throw new UserFriendlyException("Facility ID is required");
      }

      facility = facilityFacade.find(facilityId);

      if (facility == null) {
        throw new UserFriendlyException("Facility not found with ID: " + facilityId);
      }

      comments = request.getParameter("comments");

      try {
        sendNotifications = ParamConverter.convertYNBoolean(request, "notification");

        if (sendNotifications == null) {
          sendNotifications = false;
        }
      } catch (Exception e) {
        throw new UserFriendlyException("Unable to parse notifications parameter");
      }

      List<BeamDestinationAuthorization> beamDestinationAuthorizationList =
          convertDestinationAuthorizationList(facility, request);

      beamAuthorizationFacade.saveAuthorization(comments, beamDestinationAuthorizationList);
    } catch (UserFriendlyException e) {
      errorReason = e.getUserMessage();
      LOGGER.log(Level.INFO, "Unable to save authorization: " + errorReason);
    } catch (Exception e) {
      errorReason = "Unable to save authorization";
      LOGGER.log(Level.SEVERE, errorReason, e);
    }

    if (errorReason == null && sendNotifications) {
      String proxyServer = System.getenv("FRONTEND_SERVER_URL");

      try {
        beamAuthorizationFacade.sendOpsNewAuthorizationEmail(proxyServer, comments);
      } catch (UserFriendlyException e) {
        errorReason = "Authorization was saved, but we were unable to send to ops an email.  ";
        LOGGER.log(Level.SEVERE, errorReason, e);
      }

      try {
        String logbookServer = System.getenv("LOGBOOK_SERVER_URL");

        logId = beamAuthorizationFacade.sendELog(facility, proxyServer, logbookServer);
      } catch (Exception e) {
        errorReason = "Authorization was saved, but we were unable to send to eLog";
        LOGGER.log(Level.SEVERE, errorReason, e);
      }
    }

    response.setContentType("application/json");

    OutputStream out = response.getOutputStream();

    try (JsonGenerator gen = Json.createGenerator(out)) {
      gen.writeStartObject();
      if (errorReason == null) {
        if (logId != null) {
          gen.write("logId", logId);
        }
      } else {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        gen.write("error", errorReason);
      }
      gen.writeEnd();
    }
  }

  private List<BeamDestinationAuthorization> convertDestinationAuthorizationList(
      Facility facility, HttpServletRequest request) throws UserFriendlyException {
    List<BeamDestinationAuthorization> beamDestinationAuthorizationList = new ArrayList<>();
    String[] modeArray = request.getParameterValues("mode[]");
    String[] limitStrArray = request.getParameterValues("limit[]");
    String[] commentsArray = request.getParameterValues("comment[]");
    String[] expirationArray = request.getParameterValues("expiration[]");
    String[] beamDestinationIdStrArray = request.getParameterValues("beamDestinationId[]");
    if (modeArray != null) {
      if (limitStrArray == null || limitStrArray.length != modeArray.length) {
        throw new IllegalArgumentException("mode array and limit array are of different length");
      }

      if (beamDestinationIdStrArray == null
          || beamDestinationIdStrArray.length != modeArray.length) {
        throw new IllegalArgumentException(
            "mode array and beam destination ID array are of different length");
      }

      SimpleDateFormat dateFormatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

      for (int i = 0; i < modeArray.length; i++) {
        String mode = modeArray[i];

        BeamDestinationAuthorization da = new BeamDestinationAuthorization();

        da.setBeamMode(mode);

        String limitStr = limitStrArray[i];

        if ("None".equals(mode)) {
          da.setCwLimit(null);
        } else if (limitStr != null && !limitStr.equals("") && !limitStr.equals("N/A")) {
          limitStr = limitStr.replaceAll(",", ""); // Remove commas

          try {
            BigDecimal limit = new BigDecimal(limitStr);
            da.setCwLimit(limit);
          } catch (NumberFormatException e) {
            throw new UserFriendlyException("limit must be a number", e);
          }
        }

        String comments = commentsArray[i];
        da.setComments(comments);

        String expiration = expirationArray[i];
        if (expiration != null && !expiration.trim().isEmpty()) {
          try {
            Date expirationDate = dateFormatter.parse(expiration);
            da.setExpirationDate(expirationDate);
          } catch (ParseException e) {
            LOGGER.log(Level.WARNING, "Unable to parse expiration date", e);
          }
        }

        String beamDestinationIdStr = beamDestinationIdStrArray[i];

        if (beamDestinationIdStr == null) {
          throw new IllegalArgumentException("Beam Destination ID must not be null");
        }

        BigInteger beamDestinationId = new BigInteger(beamDestinationIdStr);

        // TODO: Check if Beam Destination exists with given ID and verify it matches Facility

        DestinationAuthorizationPK pk = new DestinationAuthorizationPK();
        pk.setBeamDestinationId(beamDestinationId);
        da.setDestinationAuthorizationPK(pk);

        beamDestinationAuthorizationList.add(da);
      }
    }

    return beamDestinationAuthorizationList;
  }
}
