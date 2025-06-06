package org.jlab.jam.presentation.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.jam.business.session.*;
import org.jlab.jam.persistence.entity.*;
import org.jlab.jam.persistence.enumeration.OperationsType;
import org.jlab.jam.persistence.view.FacilityExpirationEvent;

/**
 * @author ryans
 */
@WebServlet(
    name = "FacilityAuthorization",
    urlPatterns = {"/authorizations/*"})
public class FacilityAuthorization extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(FacilityAuthorization.class.getName());
  @EJB RFAuthorizationFacade rfAuthorizationFacade;
  @EJB BeamAuthorizationFacade beamAuthorizationFacade;
  @EJB BeamDestinationFacade beamDestinationFacade;
  @EJB FacilityFacade facilityFacade;
  @EJB RFSegmentFacade rfSegmentFacade;
  @EJB ExpirationManager expirationManager;
  @EJB NotificationManager notificationManager;
  @EJB AuthorizerFacade authorizerFacade;

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

    String pathInfo = request.getPathInfo();

    if (pathInfo == null || pathInfo.isEmpty()) {
      throw new ServletException("Path is empty");
    }

    Path path = Paths.get(pathInfo);

    if (path.getNameCount() == 0) {
      throw new ServletException("Path is root only");
    }

    // WARNING: String.endWith("/") and Path.endWith("/") return DIFFERENT results!
    if (pathInfo.endsWith("/")) {
      // TODO: probably redirect?
      throw new ServletException("Path ends with /");
    }

    String facilityPath = "/" + path.getName(0);

    Facility facility = facilityFacade.findByPath(facilityPath);

    if (facility == null) {
      throw new ServletException("Facility not found");
    }

    List<Facility> facilityList =
        facilityFacade.findAll(new AbstractFacade.OrderDirective("weight"));

    request.setAttribute("facility", facility);
    request.setAttribute("facilityList", facilityList);

    switch (path.getNameCount()) {
      case 1:
        handleFacility(request, response, facility);
        break;
      case 2:
        String secondName = path.getName(1).toString();

        if ("beam-history".equals(secondName)) {
          getServletContext()
              .getNamedDispatcher("BeamAuthorizationHistoryController")
              .forward(request, response);
        } else if ("rf-history".equals(secondName)) {
          getServletContext()
              .getNamedDispatcher("RFAuthorizationHistoryController")
              .forward(request, response);
        } else {
          // TODO: This should probably be 404
          throw new ServletException("Unknown authorizations path: " + pathInfo);
        }
        break;
      case 3:
        String thirdName = path.getName(2).toString();

        if ("destinations".equals(thirdName)) {
          getServletContext()
              .getNamedDispatcher("DestinationsAuthorizationHistoryController")
              .forward(request, response);
        } else if ("segments".equals(thirdName)) {
          getServletContext()
              .getNamedDispatcher("SegmentsAuthorizationHistoryController")
              .forward(request, response);
        } else {
          // TODO: This should probably be 404
          throw new ServletException("Unknown authorizations path: " + pathInfo);
        }
        break;
      default:
        throw new ServletException("Path has too many segments");
    }
  }

  private void handleFacility(
      HttpServletRequest request, HttpServletResponse response, Facility facility)
      throws ServletException, IOException {

    try {
      FacilityExpirationEvent event = expirationManager.expireByFacility(facility);

      notificationManager.asyncNotifyFacilityExpiration(event);
    } catch (InterruptedException e) {
      throw new ServletException("Unable to expire", e);
    }

    RFAuthorization rfAuthorization = rfAuthorizationFacade.findCurrent(facility);
    BeamAuthorization beamAuthorization = beamAuthorizationFacade.findCurrent(facility);

    List<RFSegment> rfList = rfSegmentFacade.filterList(true, facility, null);
    List<BeamDestination> beamList = beamDestinationFacade.filterList(true, facility, null);

    Map<BigInteger, BeamDestinationAuthorization> destinationAuthorizationMap =
        beamAuthorizationFacade.createDestinationAuthorizationMap(beamAuthorization);

    Map<BigInteger, RFSegmentAuthorization> segmentAuthorizationMap =
        rfAuthorizationFacade.createSegmentAuthorizationMap(rfAuthorization);

    String username = request.getRemoteUser();

    boolean isRfEditable = false;
    boolean isBeamEditable = false;

    if (username != null) {
      if (request.isUserInRole("jam-admin")) {
        isRfEditable = true;
        isBeamEditable = true;
      } else {
        isRfEditable = authorizerFacade.isAuthorizerBool(facility, OperationsType.RF, username);
        isBeamEditable = authorizerFacade.isAuthorizerBool(facility, OperationsType.BEAM, username);
      }
    }

    request.setAttribute("unitsMap", beamAuthorizationFacade.getUnitsMap());
    request.setAttribute("isRfEditable", isRfEditable);
    request.setAttribute("isBeamEditable", isBeamEditable);
    request.setAttribute("rfAuthorization", rfAuthorization);
    request.setAttribute("beamAuthorization", beamAuthorization);
    request.setAttribute("rfList", rfList);
    request.setAttribute("beamList", beamList);
    request.setAttribute("segmentAuthorizationMap", segmentAuthorizationMap);
    request.setAttribute("destinationAuthorizationMap", destinationAuthorizationMap);

    request
        .getRequestDispatcher("/WEB-INF/views/facility-authorization.jsp")
        .forward(request, response);
  }
}
