package org.jlab.jam.presentation.controller.ajax;

import java.io.IOException;
import java.io.OutputStream;
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
import org.jlab.jam.business.session.*;
import org.jlab.jam.persistence.entity.*;
import org.jlab.jam.persistence.enumeration.OperationsType;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "EditRFAuthorization",
    urlPatterns = {"/ajax/edit-rf-auth"})
public class EditRFAuthorization extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(EditRFAuthorization.class.getName());

  @EJB RFAuthorizationFacade rfAuthorizationFacade;
  @EJB FacilityFacade facilityFacade;
  @EJB LogbookFacade logbookFacade;
  @EJB EmailFacade emailFacade;

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
    Boolean sendNotifications = true;
    BigInteger rfAuthorizationId = null;

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

      List<RFSegmentAuthorization> rfSegmentAuthorizationList =
          convertSegmentAuthorizationList(facility, request);

      rfAuthorizationId =
          rfAuthorizationFacade.saveAuthorization(facility, comments, rfSegmentAuthorizationList);
    } catch (UserFriendlyException e) {
      errorReason = e.getUserMessage();
      LOGGER.log(Level.INFO, "Unable to save authorization: " + errorReason);
    } catch (Exception e) {
      errorReason = "Unable to save authorization";
      LOGGER.log(Level.SEVERE, errorReason, e);
    }

    if (errorReason == null && sendNotifications) {
      String proxyServer = System.getenv("FRONTEND_SERVER_URL");

      emailFacade.sendAsyncAuthorizerChangeEmail(OperationsType.RF, rfAuthorizationId);

      try {
        String logbookServer = System.getenv("LOGBOOK_SERVER_URL");

        logId =
            logbookFacade.sendAuthorizationLogEntry(
                facility, OperationsType.RF, proxyServer, logbookServer);

        rfAuthorizationFacade.setLogEntry(rfAuthorizationId, logId, logbookServer);
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

  private List<RFSegmentAuthorization> convertSegmentAuthorizationList(
      Facility facility, HttpServletRequest request) throws UserFriendlyException {
    List<RFSegmentAuthorization> rfSegmentAuthorizationList = new ArrayList<>();
    String[] modeArray = request.getParameterValues("mode[]");
    String[] commentsArray = request.getParameterValues("comment[]");
    String[] expirationArray = request.getParameterValues("expiration[]");
    String[] rfSegmentIdStrArray = request.getParameterValues("rfSegmentId[]");
    if (modeArray != null) {
      if (rfSegmentIdStrArray == null || rfSegmentIdStrArray.length != modeArray.length) {
        throw new IllegalArgumentException(
            "mode array and RF Segment ID array are of different length");
      }

      SimpleDateFormat dateFormatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

      for (int i = 0; i < modeArray.length; i++) {
        String mode = modeArray[i];

        boolean highPowerRf = "Yes".equals(mode);

        RFSegmentAuthorization da = new RFSegmentAuthorization();

        da.setHighPowerRf(highPowerRf);

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

        String rfSegmentIdStr = rfSegmentIdStrArray[i];

        if (rfSegmentIdStr == null) {
          throw new IllegalArgumentException("RF Segment ID must not be null");
        }

        BigInteger rfSegmentId = new BigInteger(rfSegmentIdStr);

        // We don't Check if RF Segment exists with given ID and verify matches
        // SegmentAuthorization Facility
        // At the moment we have database constraints that will check this for us, but error may be
        // hard to parse
        da.setFacility(facility);

        SegmentAuthorizationPK pk = new SegmentAuthorizationPK();
        pk.setRFSegmentId(rfSegmentId);
        da.setSegmentAuthorizationPK(pk);

        rfSegmentAuthorizationList.add(da);
      }
    }

    return rfSegmentAuthorizationList;
  }
}
