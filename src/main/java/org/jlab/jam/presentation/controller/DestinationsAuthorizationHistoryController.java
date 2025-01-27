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
import org.jlab.jam.persistence.entity.BeamAuthorization;
import org.jlab.jam.persistence.entity.BeamDestination;
import org.jlab.jam.persistence.entity.BeamDestinationAuthorization;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "DestinationsAuthorizationHistoryController",
    urlPatterns = {"/permissions/destinations-authorization-history"})
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

    BigInteger authorizationId = ParamConverter.convertBigInteger(request, "authorizationId");

    BeamAuthorization beamAuthorization = null;

    if (authorizationId != null) {
      beamAuthorization = beamAuthorizationFacade.find(authorizationId);
    }

    List<BeamDestination> cebafDestinationList = beamDestinationFacade.findCebafDestinations();
    List<BeamDestination> lerfDestinationList = beamDestinationFacade.findLerfDestinations();
    List<BeamDestination> uitfDestinationList = beamDestinationFacade.findUitfDestinations();

    Map<BigInteger, BeamDestinationAuthorization> destinationAuthorizationMap =
        beamAuthorizationFacade.createDestinationAuthorizationMap(beamAuthorization);

    request.setAttribute("unitsMap", beamAuthorizationFacade.getUnitsMap());
    request.setAttribute("authorization", beamAuthorization);
    request.setAttribute("cebafDestinationList", cebafDestinationList);
    request.setAttribute("lerfDestinationList", lerfDestinationList);
    request.setAttribute("uitfDestinationList", uitfDestinationList);
    request.setAttribute("destinationAuthorizationMap", destinationAuthorizationMap);

    request
        .getRequestDispatcher("/WEB-INF/views/permissions/destinations-authorization-history.jsp")
        .forward(request, response);
  }
}
