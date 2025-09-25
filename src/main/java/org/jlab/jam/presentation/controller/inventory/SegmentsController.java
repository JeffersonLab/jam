package org.jlab.jam.presentation.controller.inventory;

import static org.jlab.jam.business.session.AbstractFacade.OrderDirective;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.jlab.jam.business.session.RFSegmentFacade;
import org.jlab.jam.persistence.entity.RFSegment;

/**
 * @author ryans
 */
@WebServlet(
    name = "SegmentsController",
    urlPatterns = {"/inventory/segments"})
public class SegmentsController extends HttpServlet {

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
    OrderDirective[] order =
        new OrderDirective[] {new OrderDirective("facility"), new OrderDirective("weight")};

    List<RFSegment> segmentList = segmentFacade.findAll(order);

    request.setAttribute("segmentList", segmentList);

    request
        .getRequestDispatcher("/WEB-INF/views/inventory/segments.jsp")
        .forward(request, response);
  }
}
