package org.jlab.jam.presentation.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.jam.business.session.BeamAuthorizationFacade;
import org.jlab.jam.business.session.BeamDestinationFacade;
import org.jlab.jam.persistence.entity.*;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(name = "DestinationsAuthorizationHistoryController")
public class DestinationsAuthorizationHistoryController extends HttpServlet {

  @EJB BeamAuthorizationFacade beamAuthorizationFacade;
  @EJB BeamDestinationFacade beamDestinationFacade;

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

    BigInteger beamAuthorizationId =
        ParamConverter.convertBigInteger(request, "beamAuthorizationId");

    BeamAuthorization beamAuthorization = null;

    if (beamAuthorizationId != null) {
      beamAuthorization = beamAuthorizationFacade.find(beamAuthorizationId);
    }

    Facility facility = (Facility) request.getAttribute("facility");

    // TODO: Instead of querying for current list of destinations / segments then mapping to
    // authorization,
    // it would be better to grab list of destinations and segments that existed at time of
    // authorization.
    List<BeamDestination> beamList = beamDestinationFacade.filterList(true, facility);

    Map<BigInteger, BeamDestinationAuthorization> destinationAuthorizationMap =
        beamAuthorizationFacade.createDestinationAuthorizationMap(beamAuthorization);

    request.setAttribute("unitsMap", beamAuthorizationFacade.getUnitsMap());
    request.setAttribute("beamAuthorization", beamAuthorization);
    request.setAttribute("beamList", beamList);
    request.setAttribute("destinationAuthorizationMap", destinationAuthorizationMap);

    request
        .getRequestDispatcher("/WEB-INF/views/authorization-history/destinations-history.jsp")
        .forward(request, response);
  }
}
