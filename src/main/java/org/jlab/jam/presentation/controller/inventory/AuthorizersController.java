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
import org.jlab.jam.business.session.AuthorizerFacade;
import org.jlab.jam.business.session.FacilityFacade;
import org.jlab.jam.persistence.entity.Authorizer;
import org.jlab.jam.persistence.entity.Facility;

/**
 * @author ryans
 */
@WebServlet(
    name = "AuthorizersController",
    urlPatterns = {"/inventory/authorizers"})
public class AuthorizersController extends HttpServlet {

  @EJB FacilityFacade facilityFacade;
  @EJB AuthorizerFacade authorizerFacade;

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
    List<Authorizer> authorizerList = authorizerFacade.filterList(null, null);

    request.setAttribute("facilityList", facilityList);
    request.setAttribute("authorizerList", authorizerList);

    request
        .getRequestDispatcher("/WEB-INF/views/inventory/authorizers.jsp")
        .forward(request, response);
  }
}
