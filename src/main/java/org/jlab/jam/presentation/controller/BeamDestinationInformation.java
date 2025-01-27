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
import org.jlab.jam.business.session.BeamControlVerificationFacade;
import org.jlab.jam.business.session.BeamDestinationFacade;
import org.jlab.jam.persistence.entity.BeamControlVerification;
import org.jlab.jam.persistence.entity.BeamDestination;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "BeamDestinationInformation",
    urlPatterns = {"/beam-destination-information"})
public class BeamDestinationInformation extends HttpServlet {

  @EJB BeamDestinationFacade beamDestinationFacade;
  @EJB BeamControlVerificationFacade verificationFacade;

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

    BigInteger beamDestinationId = ParamConverter.convertBigInteger(request, "beamDestinationId");

    BeamDestination beamDestination = null;
    List<BeamControlVerification> verificationList = null;

    if (beamDestinationId != null) {
      beamDestination = beamDestinationFacade.find(beamDestinationId);
      verificationList = verificationFacade.findByBeamDestination(beamDestinationId);
    }

    request.setAttribute("beamDestination", beamDestination);
    request.setAttribute("verificationList", verificationList);

    request
        .getRequestDispatcher("WEB-INF/views/beam-destination-information.jsp")
        .forward(request, response);
  }
}
