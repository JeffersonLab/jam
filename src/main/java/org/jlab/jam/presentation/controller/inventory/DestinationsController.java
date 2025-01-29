package org.jlab.jam.presentation.controller.inventory;

import static org.jlab.jam.business.session.AbstractFacade.OrderDirective;

import java.io.IOException;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.jam.business.session.BeamDestinationFacade;
import org.jlab.jam.persistence.entity.BeamDestination;

/**
 * @author ryans
 */
@WebServlet(
    name = "DestinationsController",
    urlPatterns = {"/inventory/destinations"})
public class DestinationsController extends HttpServlet {

  @EJB BeamDestinationFacade destinationFacade;

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

    List<BeamDestination> destinationList = destinationFacade.findAll(order);

    request.setAttribute("destinationList", destinationList);

    request
        .getRequestDispatcher("/WEB-INF/views/inventory/destinations.jsp")
        .forward(request, response);
  }
}
