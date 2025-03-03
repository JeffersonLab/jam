package org.jlab.jam.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.jam.business.session.BeamControlVerificationFacade;
import org.jlab.jam.business.session.RFControlVerificationFacade;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "EditOperationsVerifications",
    urlPatterns = {"/ajax/edit-operations-verifications"})
public class EditOperationsVerifications extends HttpServlet {

  private static final Logger logger =
      Logger.getLogger(EditOperationsVerifications.class.getName());
  @EJB BeamControlVerificationFacade beamVerificationFacade;
  @EJB RFControlVerificationFacade rfVerificationFacade;

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
      BigInteger[] verificationIdArray =
          ParamConverter.convertBigIntegerArray(request, "verificationIdArray[]");
      Integer verificationId = ParamConverter.convertInteger(request, "verificationId");
      Date verificationDate = ParamConverter.convertFriendlyDateTime(request, "verificationDate");
      String verifiedByUsername = request.getParameter("verifiedBy");
      Date expirationDate = ParamConverter.convertFriendlyDateTime(request, "expirationDate");
      String comments = request.getParameter("comments");
      String externalUrl = request.getParameter("externalUrl");
      String type = request.getParameter("verificationType");

      if ("BEAM".equals(type)) {
        beamVerificationFacade.edit(
            verificationIdArray,
            verificationId,
            verificationDate,
            verifiedByUsername,
            expirationDate,
            comments,
            externalUrl);
      } else if ("RF".equals(type)) {
        rfVerificationFacade.edit(
            verificationIdArray,
            verificationId,
            verificationDate,
            verifiedByUsername,
            expirationDate,
            comments,
            externalUrl);
      } else {
        throw new UserFriendlyException("Unknown verification type: " + type);
      }
    } catch (UserFriendlyException e) {
      errorReason = e.getUserMessage();
      logger.log(Level.FINE, "Unable to edit control verification", e);
    } catch (Exception e) {
      errorReason = "Unable to edit control verification";
      logger.log(Level.SEVERE, errorReason, e);
    }

    response.setContentType("text/xml");

    PrintWriter pw = response.getWriter();

    String xml = "";

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
