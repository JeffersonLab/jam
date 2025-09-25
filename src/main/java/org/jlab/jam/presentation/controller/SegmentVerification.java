package org.jlab.jam.presentation.controller;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import org.jlab.jam.business.session.RFSegmentFacade;
import org.jlab.jam.persistence.entity.RFSegment;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "SegmentVerification",
    urlPatterns = {"/verifications/segment"})
public class SegmentVerification extends HttpServlet {

  @EJB RFSegmentFacade segmentFacade;

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

    BigInteger segmentId = ParamConverter.convertBigInteger(request, "segmentId");

    RFSegment segment = null;
    boolean adminOrLeader = false;

    if (segmentId != null) {
      segment = segmentFacade.findWithVerificationList(segmentId);
      adminOrLeader = request.getRemoteUser() != null;
    }

    List<RFSegment> segmentList = segmentFacade.filterList(true, null, null);

    request.setAttribute("segmentList", segmentList);
    request.setAttribute("adminOrLeader", adminOrLeader);
    request.setAttribute("segment", segment);

    request
        .getRequestDispatcher("/WEB-INF/views/segment-verification.jsp")
        .forward(request, response);
  }
}
