package org.jlab.jam.presentation.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.jam.business.session.AuthorizationFacade;
import org.jlab.jam.business.session.BeamDestinationFacade;
import org.jlab.jam.business.session.ControlVerificationFacade;
import org.jlab.jam.persistence.entity.Authorization;
import org.jlab.jam.persistence.entity.BeamDestination;
import org.jlab.jam.persistence.entity.DestinationAuthorization;
import org.jlab.jam.persistence.entity.DestinationAuthorizationPK;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "BeamPermissions",
    urlPatterns = {"/permissions"})
public class BeamPermissions extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(BeamPermissions.class.getName());
  @EJB AuthorizationFacade authorizationFacade;
  @EJB BeamDestinationFacade beamDestinationFacade;
  @EJB ControlVerificationFacade verificationFacade;

  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    verificationFacade.performExpirationCheck(false);

    Authorization authorization = authorizationFacade.findCurrent();

    List<BeamDestination> cebafDestinationList = beamDestinationFacade.findCebafDestinations();
    List<BeamDestination> lerfDestinationList = beamDestinationFacade.findLerfDestinations();
    List<BeamDestination> uitfDestinationList = beamDestinationFacade.findUitfDestinations();

    Map<BigInteger, DestinationAuthorization> destinationAuthorizationMap =
        authorizationFacade.createDestinationAuthorizationMap(authorization);

    request.setAttribute("unitsMap", authorizationFacade.getUnitsMap());
    request.setAttribute("authorization", authorization);
    request.setAttribute("cebafDestinationList", cebafDestinationList);
    request.setAttribute("lerfDestinationList", lerfDestinationList);
    request.setAttribute("uitfDestinationList", uitfDestinationList);
    request.setAttribute("destinationAuthorizationMap", destinationAuthorizationMap);

    request.getRequestDispatcher("WEB-INF/views/permissions.jsp").forward(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  @SuppressWarnings("unchecked")
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String errorReason = null;
    Long logId = null;

    String comments = request.getParameter("comments");

    Boolean sendNotifications = false;

    try {
      sendNotifications = ParamConverter.convertYNBoolean(request, "notification");

      if (sendNotifications == null) {
        sendNotifications = false;
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Unable to parse notifications parameter");
    }

    try {
      List<DestinationAuthorization> destinationAuthorizationList =
          convertDestinationAuthorizationList(request);

      authorizationFacade.saveAuthorization(comments, destinationAuthorizationList);
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
        authorizationFacade.sendOpsNewAuthorizationEmail(proxyServer, comments);
      } catch (UserFriendlyException e) {
        errorReason = "Authorization was saved, but we were unable to send to ops an email.  ";
        LOGGER.log(Level.SEVERE, errorReason, e);
      }

      try {
        String logbookServer = System.getenv("LOGBOOK_SERVER_URL");

        logId = authorizationFacade.sendELog(proxyServer, logbookServer);
      } catch (Exception e) {
        errorReason = "Authorization was saved, but we were unable to send to eLog";
        LOGGER.log(Level.SEVERE, errorReason, e);
      }
    }

    response.setContentType("application/json");

    PrintWriter pw = response.getWriter();

    JsonObjectBuilder builder = Json.createObjectBuilder();

    if (errorReason == null) {
      if (logId != null) {
        builder.add("logId", logId);
      }
    } else {
      builder.add("error", errorReason);
    }

    JsonObject obj = builder.build();

    String json = obj.toString();

    pw.print(json);

    pw.flush();

    boolean error = pw.checkError();

    if (error) {
      LOGGER.log(Level.SEVERE, "PrintWriter Error");
    }
  }

  private List<DestinationAuthorization> convertDestinationAuthorizationList(
      HttpServletRequest request) throws UserFriendlyException {
    List<DestinationAuthorization> destinationAuthorizationList = new ArrayList<>();
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

        DestinationAuthorization da = new DestinationAuthorization();

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

        DestinationAuthorizationPK pk = new DestinationAuthorizationPK();
        pk.setBeamDestinationId(beamDestinationId);
        da.setDestinationAuthorizationPK(pk);

        destinationAuthorizationList.add(da);
      }
    }

    return destinationAuthorizationList;
  }
}
