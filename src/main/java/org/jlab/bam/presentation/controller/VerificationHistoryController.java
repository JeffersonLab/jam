package org.jlab.bam.presentation.controller;

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
import org.jlab.bam.business.session.ControlVerificationFacade;
import org.jlab.bam.business.session.CreditedControlFacade;
import org.jlab.bam.business.session.VerificationHistoryFacade;
import org.jlab.bam.persistence.entity.ControlVerification;
import org.jlab.bam.persistence.entity.VerificationHistory;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "VerificationHistoryController",
    urlPatterns = {"/credited-controls/verification-history"})
public class VerificationHistoryController extends HttpServlet {

  @EJB VerificationHistoryFacade historyFacade;
  @EJB ControlVerificationFacade verificationFacade;
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

    BigInteger controlVerificationId =
        ParamConverter.convertBigInteger(request, "controlVerificationId");
    int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
    int maxPerPage = 10;

    ControlVerification verification =
        verificationFacade.findWithCreditedControl(controlVerificationId);

    List<VerificationHistory> historyList =
        historyFacade.findHistory(controlVerificationId, offset, maxPerPage);
    Long totalRecords = historyFacade.countHistory(controlVerificationId);

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
        .getRequestDispatcher("/WEB-INF/views/credited-controls/verification-history.jsp")
        .forward(request, response);
  }
}
