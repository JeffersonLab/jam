package org.jlab.jam.presentation.controller.inventory;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
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
