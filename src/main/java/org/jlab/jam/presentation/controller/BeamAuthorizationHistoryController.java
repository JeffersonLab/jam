package org.jlab.jam.presentation.controller;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.jam.business.session.BeamAuthorizationFacade;
import org.jlab.jam.persistence.entity.BeamAuthorization;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(name = "BeamAuthorizationHistoryController")
public class BeamAuthorizationHistoryController extends HttpServlet {

  @EJB BeamAuthorizationFacade historyFacade;

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

    int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
    int maxPerPage = 10;

    List<BeamAuthorization> historyList = historyFacade.findHistory(offset, maxPerPage);
    Long totalRecords = historyFacade.countHistory();

    Paginator paginator = new Paginator(totalRecords.intValue(), offset, maxPerPage);

    DecimalFormat formatter = new DecimalFormat("###,###");

    String selectionMessage = "All Authorizations";

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
    request.setAttribute("historyList", historyList);
    request.setAttribute("paginator", paginator);

    request
        .getRequestDispatcher("/WEB-INF/views/history/beam-history.jsp")
        .forward(request, response);
  }
}
