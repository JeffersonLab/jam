package org.jlab.jam.presentation.controller.inventory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.jam.business.session.*;
import org.jlab.jam.persistence.entity.BeamDestination;
import org.jlab.jam.persistence.entity.CreditedControl;
import org.jlab.jam.persistence.entity.Facility;
import org.jlab.jam.persistence.entity.RFSegment;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "ParticipationController",
    urlPatterns = {"/inventory/participation"})
public class ParticipationController extends HttpServlet {

  @EJB FacilityFacade facilityFacade;
  @EJB CreditedControlFacade ccFacade;
  @EJB RFSegmentFacade segmentFacade;
  @EJB BeamDestinationFacade destinationFacade;

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

    String error = null;

    BigInteger facilityId = ParamConverter.convertBigInteger(request, "facilityId");

    if (facilityId == null) {
      error = "Select a Facility to continue";
    } else {
      Facility facility = facilityFacade.find(facilityId);

      if (facility == null) {
        throw new ServletException("Facility not found");
      }

      List<RFSegment> segmentList = segmentFacade.findByFacility(facility);
      List<BeamDestination> destinationList = destinationFacade.findByFacility(facility);
      List<CreditedControl> ccList = ccFacade.findAllWithVerificationList();

      request.setAttribute("facility", facility);
      request.setAttribute("segmentList", segmentList);
      request.setAttribute("destinationList", destinationList);
      request.setAttribute("ccList", ccList);
    }

    List<Facility> facilityList =
        facilityFacade.findAll(new AbstractFacade.OrderDirective("weight"));

    request.setAttribute("facilityList", facilityList);
    request.setAttribute("error", error);

    request
        .getRequestDispatcher("/WEB-INF/views/inventory/participation.jsp")
        .forward(request, response);
  }
}
