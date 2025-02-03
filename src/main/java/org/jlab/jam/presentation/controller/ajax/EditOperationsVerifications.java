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
import org.jlab.jam.business.session.BeamControlVerificationFacade;
import org.jlab.jam.business.session.RFControlVerificationFacade;
import org.jlab.jam.persistence.entity.BeamControlVerification;
import org.jlab.jam.persistence.entity.RFControlVerification;
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

    List<BeamControlVerification> beamDowngradeList = null;
    List<RFControlVerification> rfDowngradeList = null;

    try {
      BigInteger[] verificationIdArray =
          ParamConverter.convertBigIntegerArray(request, "verificationIdArray[]");
      Integer verificationId = ParamConverter.convertInteger(request, "verificationId");
      Date verificationDate = ParamConverter.convertFriendlyDateTime(request, "verificationDate");
      String verifiedByUsername = request.getParameter("verifiedBy");
      Date expirationDate = ParamConverter.convertFriendlyDateTime(request, "expirationDate");
      String comments = request.getParameter("comments");
      String type = request.getParameter("verificationType");

      if ("BEAM".equals(type)) {
        beamDowngradeList =
            beamVerificationFacade.edit(
                verificationIdArray,
                verificationId,
                verificationDate,
                verifiedByUsername,
                expirationDate,
                comments);
      } else if ("RF".equals(type)) {
        rfDowngradeList =
            rfVerificationFacade.edit(
                verificationIdArray,
                verificationId,
                verificationDate,
                verifiedByUsername,
                expirationDate,
                comments);
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

    CorrespondenceResult result = null;
    String proxyServer = System.getenv("FRONTEND_SERVER_URL");
    String logbookServer = System.getenv("LOGBOOK_SERVER_URL");

    if (errorReason == null) {
      if (beamDowngradeList != null && !beamDowngradeList.isEmpty()) {
        result = handleBeamCorrespondence(beamDowngradeList, proxyServer, logbookServer);
      } else if (rfDowngradeList != null && !rfDowngradeList.isEmpty()) {
        result = handleRFCorrespondence(rfDowngradeList, proxyServer, logbookServer);
      } else {
        // Nothing was downgraded, so no need for correspondence
      }
    }

    response.setContentType("text/xml");

    PrintWriter pw = response.getWriter();

    String xml;

    if (errorReason == null) { // no error saving to DB
      if (result == null) { // no need for correspondence
        xml = "<response><span class=\"status\">Success</span>" + "</response>";
      } else { // Attempted correspondence
        if (result.emailErrorReason == null && result.elogErrorReason == null) {
          xml =
              "<response><span class=\"status\">Success</span><span class=\"logid\">"
                  + (result.logId == null ? "" : result.logId)
                  + "</span></response>";
        } else {
          xml =
              "<response><span class=\"status\">PartialSuccess</span><span class=\"logid\">"
                  + (result.logId == null ? "" : result.logId)
                  + "</span>"
                  + "<span class=\"elog-error\">"
                  + (result.elogErrorReason == null ? "" : result.elogErrorReason)
                  + "</span>"
                  + "<span class=\"email-error\">"
                  + (result.emailErrorReason == null ? "" : result.emailErrorReason)
                  + "</span>"
                  + "</response>";
        }
      }
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

  private CorrespondenceResult handleRFCorrespondence(
      List<RFControlVerification> rfDowngradeList, String proxyServer, String logbookServer) {
    CorrespondenceResult result = new CorrespondenceResult();

    String body =
        rfVerificationFacade.getVerificationDowngradedMessageBody(proxyServer, rfDowngradeList);

    try {
      result.logId = rfVerificationFacade.sendVerificationDowngradedELog(body, logbookServer);
    } catch (Exception e) {
      result.elogErrorReason = "Edit saved, but unable to create elog";
      logger.log(Level.SEVERE, result.elogErrorReason, e);
    }

    try {
      rfVerificationFacade.sendVerificationDowngradedEmail(body);
    } catch (Exception e) {
      result.emailErrorReason = "Edit saved and elog entry created, but unable to send email";
      logger.log(Level.SEVERE, result.emailErrorReason, e);
    }

    return result;
  }

  private CorrespondenceResult handleBeamCorrespondence(
      List<BeamControlVerification> beamDowngradeList, String proxyServer, String logbookServer) {
    CorrespondenceResult result = new CorrespondenceResult();

    String body =
        beamVerificationFacade.getVerificationDowngradedMessageBody(proxyServer, beamDowngradeList);

    try {
      result.logId = beamVerificationFacade.sendVerificationDowngradedELog(body, logbookServer);
    } catch (Exception e) {
      result.elogErrorReason = "Edit saved, but unable to create elog";
      logger.log(Level.SEVERE, result.elogErrorReason, e);
    }

    try {
      beamVerificationFacade.sendVerificationDowngradedEmail(body);
    } catch (Exception e) {
      result.emailErrorReason = "Edit saved and elog entry created, but unable to send email";
      logger.log(Level.SEVERE, result.emailErrorReason, e);
    }

    return result;
  }

  public static class CorrespondenceResult {
    public String emailErrorReason = null;
    public String elogErrorReason = null;
    public Long logId = null;
  }
}
