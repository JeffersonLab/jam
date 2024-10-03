package org.jlab.bam.presentation.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.bam.business.session.BeamDestinationFacade;
import org.jlab.bam.persistence.entity.BeamDestination;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "Destinations",
    urlPatterns = {"/destinations"})
public class Destinations extends HttpServlet {

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

    BigInteger destinationId = ParamConverter.convertBigInteger(request, "destinationId");

    BeamDestination destination = null;
    boolean adminOrLeader = false;

    if (destinationId != null) {
      destination = destinationFacade.findWithVerificationList(destinationId);
      adminOrLeader = request.getRemoteUser() != null;
      // adminOrLeader = ccFacade.isAdminOrGroupLeader(request.getRemoteUser(),
      // creditedControl.getWorkgroup().getWorkgroupId());
    }

    List<BeamDestination> destinationList = destinationFacade.findActiveDestinations();

    request.setAttribute("destinationList", destinationList);
    request.setAttribute("adminOrLeader", adminOrLeader);
    request.setAttribute("destination", destination);

    request.getRequestDispatcher("WEB-INF/views/destinations.jsp").forward(request, response);
  }
}
