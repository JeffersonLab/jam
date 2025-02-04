package org.jlab.jam.presentation.controller.inventory;

import java.io.IOException;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.jam.business.session.WorkgroupFacade;
import org.jlab.jam.persistence.entity.Workgroup;

/**
 * @author ryans
 */
@WebServlet(
    name = "VerifiersController",
    urlPatterns = {"/inventory/verifiers"})
public class VerifiersController extends HttpServlet {

  @EJB WorkgroupFacade groupFacade;

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

    List<Workgroup> teamList = groupFacade.findWithControlsAndUsers();

    request.setAttribute("teamList", teamList);

    request
        .getRequestDispatcher("/WEB-INF/views/inventory/verifiers.jsp")
        .forward(request, response);
  }
}
