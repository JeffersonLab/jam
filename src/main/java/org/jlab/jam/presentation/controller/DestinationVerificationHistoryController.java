package org.jlab.jam.presentation.controller;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.List;
import org.jlab.jam.business.session.BeamControlVerificationFacade;
import org.jlab.jam.business.session.BeamControlVerificationHistoryFacade;
import org.jlab.jam.business.session.CreditedControlFacade;
import org.jlab.jam.persistence.entity.BeamControlVerification;
import org.jlab.jam.persistence.entity.BeamControlVerificationHistory;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "DestinationVerificationHistoryController",
    urlPatterns = {"/verifications/control/destination-history"})
public class DestinationVerificationHistoryController extends HttpServlet {

  @EJB BeamControlVerificationHistoryFacade historyFacade;
  @EJB BeamControlVerificationFacade verificationFacade;
  @EJB CreditedControlFacade creditedControlFacade;

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

    BigInteger beamControlVerificationId =
        ParamConverter.convertBigInteger(request, "beamControlVerificationId");
    int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
    int maxPerPage = 10;

    BeamControlVerification verification =
        verificationFacade.findWithCreditedControl(beamControlVerificationId);

    List<BeamControlVerificationHistory> historyList =
        historyFacade.findHistory(beamControlVerificationId, offset, maxPerPage);
    Long totalRecords = historyFacade.countHistory(beamControlVerificationId);

    Paginator paginator = new Paginator(totalRecords.intValue(), offset, maxPerPage);

    DecimalFormat formatter = new DecimalFormat("###,###");

    String selectionMessage = "All Verifications";

    if (paginator.getTotalRecords() < maxPerPage && offset == 0) {
      selectionMessage =
          selectionMessage + " {" + formatter.format(paginator.getTotalRecords()) + "}";
    } else {
      selectionMessage =
          selectionMessage
              + " {"
              + formatter.format(paginator.getStartNumber())
              + " - "
              + formatter.format(paginator.getEndNumber())
              + " of "
              + formatter.format(paginator.getTotalRecords())
              + "}";
    }

    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("verification", verification);
    request.setAttribute("historyList", historyList);
    request.setAttribute("paginator", paginator);

    request
        .getRequestDispatcher("/WEB-INF/views/verification-history/destination-history.jsp")
        .forward(request, response);
  }
}
