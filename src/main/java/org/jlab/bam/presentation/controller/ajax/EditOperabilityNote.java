package org.jlab.bam.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBAccessException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.bam.business.session.CreditedControlFacade;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "EditOperabilityNote",
    urlPatterns = {"/ajax/edit-operability-note"})
public class EditOperabilityNote extends HttpServlet {

  private static final Logger logger = Logger.getLogger(EditOperabilityNote.class.getName());
  @EJB CreditedControlFacade controlFacade;

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
      BigInteger creditedControlId = ParamConverter.convertBigInteger(request, "creditedControlId");
      String comments = request.getParameter("comments");

      controlFacade.updateComments(creditedControlId, comments);
    } catch (EJBAccessException e) {
      errorReason = "Not authorized";
      logger.log(Level.SEVERE, errorReason, e);
    } catch (Exception e) {
      errorReason = "Unable to edit operability note";
      logger.log(Level.SEVERE, errorReason, e);
    }

    response.setContentType("text/xml");

    PrintWriter pw = response.getWriter();

    String xml;

    if (errorReason == null) {
      xml = "<response><span class=\"status\">Success</span></span></response>";
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
