package org.jlab.jam.presentation.controller.inventory;

import java.io.IOException;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.jam.business.session.AbstractFacade;
import org.jlab.jam.business.session.FacilityFacade;
import org.jlab.jam.persistence.entity.Facility;

/**
 * @author ryans
 */
@WebServlet(
    name = "FacilitiesController",
    urlPatterns = {"/inventory/facilities"})
public class FacilitiesController extends HttpServlet {

  @EJB FacilityFacade facilityFacade;

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

    List<Facility> facilityList =
        facilityFacade.findAll(new AbstractFacade.OrderDirective("weight"));

    request.setAttribute("facilityList", facilityList);

    request
        .getRequestDispatcher("/WEB-INF/views/inventory/facilities.jsp")
        .forward(request, response);
  }
}
