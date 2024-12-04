package org.jlab.jam.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.jam.business.session.ControlVerificationFacade;
import org.jlab.jam.persistence.entity.ControlVerification;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "EditCc",
    urlPatterns = {"/ajax/edit-cc"})
public class EditCc extends HttpServlet {

  private static final Logger logger = Logger.getLogger(EditCc.class.getName());
  @EJB ControlVerificationFacade verificationFacade;

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

    List<ControlVerification> downgradeList = null;

    try {
      BigInteger[] verificationIdArray =
          ParamConverter.convertBigIntegerArray(request, "verificationIdArray[]");
      Integer verificationId = ParamConverter.convertInteger(request, "verificationId");
      Date verificationDate = ParamConverter.convertFriendlyDateTime(request, "verificationDate");
      String verifiedByUsername = request.getParameter("verifiedBy");
      Date expirationDate = ParamConverter.convertFriendlyDateTime(request, "expirationDate");
      String comments = request.getParameter("comments");

      downgradeList =
          verificationFacade.edit(
              verificationIdArray,
              verificationId,
              verificationDate,
              verifiedByUsername,
              expirationDate,
              comments);
    } catch (UserFriendlyException e) {
      errorReason = e.getUserMessage();
      logger.log(Level.FINE, "Unable to edit control verification", e);
    } catch (Exception e) {
      errorReason = "Unable to edit control verification";
      logger.log(Level.SEVERE, errorReason, e);
    }

    Long logId = null;

    if (errorReason == null && downgradeList != null && !downgradeList.isEmpty()) {
      String proxyServer = System.getenv("FRONTEND_SERVER_URL");
      String body =
          verificationFacade.getVerificationDowngradedMessageBody(proxyServer, downgradeList);

      try {
        String logbookServer = System.getenv("LOGBOOK_SERVER_URL");
        logId = verificationFacade.sendVerificationDowngradedELog(body, logbookServer);
      } catch (Exception e) {
        errorReason = "Edit saved, but unable to create elog";
        logger.log(Level.SEVERE, errorReason, e);
      }

      if (errorReason == null) {
        try {
          verificationFacade.sendVerificationDowngradedEmail(body);
        } catch (Exception e) {
          errorReason = "Edit saved and elog entry created, but unable to send email";
          logger.log(Level.SEVERE, errorReason, e);
        }
      }

      errorReason = null; // Telling user an email or elog wasn't sent is annoying
    }

    response.setContentType("text/xml");

    PrintWriter pw = response.getWriter();

    String xml;

    if (errorReason == null) {
      xml =
          "<response><span class=\"status\">Success</span><span class=\"logid\">"
              + (logId == null ? "" : logId)
              + "</span></response>";
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
