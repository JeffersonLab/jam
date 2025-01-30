package org.jlab.jam.presentation.controller;

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
import org.jlab.jam.business.session.AbstractFacade.OrderDirective;
import org.jlab.jam.persistence.entity.BeamControlVerification;
import org.jlab.jam.persistence.entity.BeamDestination;
import org.jlab.jam.persistence.entity.CreditedControl;
import org.jlab.jam.persistence.entity.Facility;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "VerificationsController",
    urlPatterns = {"/verifications"})
public class VerificationsController extends HttpServlet {

  @EJB CreditedControlFacade ccFacade;
  @EJB BeamControlVerificationFacade verificationFacade;
  @EJB BeamDestinationFacade destinationFacade;
  @EJB FacilityFacade facilityFacade;

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

    BigInteger facilityId = ParamConverter.convertBigInteger(request, "facilityId");

    Facility facility = null;

    if (facilityId != null) {
      facility = facilityFacade.find(facilityId);

      if (facility == null) {
        throw new ServletException("Facility not found with ID: " + facilityId);
      }
    }

    List<BeamControlVerification> expiredList = null;
    List<BeamControlVerification> expiringList = null;
    boolean adminOrLeader = false;

    expiredList = verificationFacade.checkForExpired();
    expiringList = verificationFacade.checkForUpcomingVerificationExpirations();

    List<CreditedControl> ccList = ccFacade.findAll(new OrderDirective("weight"));

    List<BeamDestination> destinationList = destinationFacade.findActiveDestinations();

    List<Facility> facilityList = facilityFacade.findAll(new OrderDirective("weight"));

    String selectionMessage = "All Verifications";

    if (facility != null) {
      selectionMessage = "Facility \"" + facility.getName() + "\"";
    }

    request.setAttribute("facility", facility);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("facilityList", facilityList);
    request.setAttribute("destinationList", destinationList);
    request.setAttribute("adminOrLeader", adminOrLeader);
    request.setAttribute("ccList", ccList);
    request.setAttribute("expiredList", expiredList);
    request.setAttribute("expiringList", expiringList);

    request.getRequestDispatcher("WEB-INF/views/verifications.jsp").forward(request, response);
  }
}
