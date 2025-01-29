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
import org.jlab.jam.business.session.CreditedControlFacade;
import org.jlab.jam.persistence.entity.CreditedControl;

/**
 * @author ryans
 */
@WebServlet(
    name = "ControlsController",
    urlPatterns = {"/inventory/controls"})
public class ControlsController extends HttpServlet {

  @EJB CreditedControlFacade controlFacade;

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

    List<CreditedControl> controlList =
        controlFacade.findAll(new AbstractFacade.OrderDirective("weight"));

    request.setAttribute("controlList", controlList);

    request
        .getRequestDispatcher("/WEB-INF/views/inventory/controls.jsp")
        .forward(request, response);
  }
}
