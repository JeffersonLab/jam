package org.jlab.jam.presentation.controller.ajax;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBAccessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.jam.business.session.BeamControlVerificationFacade;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "ToggleBeamControlParticipation",
    urlPatterns = {"/ajax/toggle-beam-control-participation"})
public class ToggleBeamControlParticipation extends HttpServlet {

  private static final Logger logger =
      Logger.getLogger(ToggleBeamControlParticipation.class.getName());

  @EJB BeamControlVerificationFacade verificationFacade;

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

    try {
      BigInteger controlId = ParamConverter.convertBigInteger(request, "creditedControlId");
      BigInteger destinationId = ParamConverter.convertBigInteger(request, "destinationId");

      verificationFacade.toggle(controlId, destinationId);
    } catch (EJBAccessException e) {
      logger.log(Level.WARNING, "Not authorized", e);
      errorReason = "Not authorized";
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to toggle", e);
      errorReason = e.getClass().getSimpleName();
    }

    response.setContentType("text/xml");

    PrintWriter pw = response.getWriter();

    String xml;

    if (errorReason == null) {
      xml = "<response><span class=\"status\">Success</span></response>";
    } else {
      xml =
          "<response><span class=\"status\">Error</span><span "
              + "class=\"reason\">"
              + errorReason
              + "</span></response>";
    }

    pw.write(xml);

    pw.flush();

    boolean error = pw.checkError();

    if (error) {
      logger.log(Level.SEVERE, "PrintWriter Error");
    }
  }
}
