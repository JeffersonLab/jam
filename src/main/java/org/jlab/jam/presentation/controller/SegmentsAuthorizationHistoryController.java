package org.jlab.jam.presentation.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.jam.business.session.RFAuthorizationFacade;
import org.jlab.jam.business.session.RFSegmentFacade;
import org.jlab.jam.persistence.entity.*;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(name = "SegmentsAuthorizationHistoryController")
public class SegmentsAuthorizationHistoryController extends HttpServlet {

  @EJB RFAuthorizationFacade rfAuthorizationFacade;
  @EJB RFSegmentFacade rfSegmentFacade;

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

    BigInteger rfAuthorizationId = ParamConverter.convertBigInteger(request, "rfAuthorizationId");

    RFAuthorization rfAuthorization = null;

    if (rfAuthorizationId != null) {
      rfAuthorization = rfAuthorizationFacade.find(rfAuthorizationId);
    }

    Facility facility = (Facility) request.getAttribute("facility");

    // TODO: Instead of querying for current list of destinations / segments then mapping to
    // authorization,
    // it would be better to grab list of destinations and segments that existed at time of
    // authorization.
    List<RFSegment> rfList = rfSegmentFacade.filterList(true, facility);

    Map<BigInteger, RFSegmentAuthorization> segmentAuthorizationMap =
        rfAuthorizationFacade.createSegmentAuthorizationMap(rfAuthorization);

    request.setAttribute("rfAuthorization", rfAuthorization);
    request.setAttribute("rfList", rfList);
    request.setAttribute("segmentAuthorizationMap", segmentAuthorizationMap);

    request
        .getRequestDispatcher("/WEB-INF/views/history/segments-history.jsp")
        .forward(request, response);
  }
}
