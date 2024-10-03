package org.jlab.bam.presentation.controller;

import java.io.IOException;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.bam.business.session.BeamDestinationFacade;
import org.jlab.bam.business.session.CreditedControlFacade;
import org.jlab.bam.persistence.entity.BeamDestination;
import org.jlab.bam.persistence.entity.CreditedControl;

/**
 * @author ryans
 */
@WebServlet(
    name = "ControlParticipation",
    urlPatterns = {"/control-participation"})
public class ControlParticipation extends HttpServlet {

  @EJB CreditedControlFacade ccFacade;
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

    List<BeamDestination> destinationList = destinationFacade.findActiveDestinations();
    List<CreditedControl> ccList = ccFacade.findAllWithVerificationList();

    request.setAttribute("destinationList", destinationList);
    request.setAttribute("ccList", ccList);

    request
        .getRequestDispatcher("WEB-INF/views/control-participation.jsp")
        .forward(request, response);
  }
}
