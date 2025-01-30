package org.jlab.jam.presentation.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.jam.business.session.CreditedControlFacade;
import org.jlab.jam.business.session.RFControlVerificationFacade;
import org.jlab.jam.business.session.RFControlVerificationHistoryFacade;
import org.jlab.jam.persistence.entity.RFControlVerification;
import org.jlab.jam.persistence.entity.RFControlVerificationHistory;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "SegmentVerificationHistoryController",
    urlPatterns = {"/verifications/control/segment-history"})
public class SegmentVerificationHistoryController extends HttpServlet {

  @EJB RFControlVerificationHistoryFacade historyFacade;
  @EJB RFControlVerificationFacade verificationFacade;
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

    BigInteger rfControlVerificationId =
        ParamConverter.convertBigInteger(request, "rfControlVerificationId");
    int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
    int maxPerPage = 10;

    RFControlVerification verification =
        verificationFacade.findWithCreditedControl(rfControlVerificationId);

    List<RFControlVerificationHistory> historyList =
        historyFacade.findHistory(rfControlVerificationId, offset, maxPerPage);
    Long totalRecords = historyFacade.countHistory(rfControlVerificationId);

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
        .getRequestDispatcher("/WEB-INF/views/verification-history/segment-history.jsp")
        .forward(request, response);
  }
}
