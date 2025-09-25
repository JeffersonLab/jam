package org.jlab.jam.presentation.controller.inventory;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.jlab.jam.business.session.VerificationTeamFacade;
import org.jlab.jam.persistence.entity.VerificationTeam;

/**
 * @author ryans
 */
@WebServlet(
    name = "VerifiersController",
    urlPatterns = {"/inventory/verifiers"})
public class VerifiersController extends HttpServlet {

  @EJB VerificationTeamFacade groupFacade;

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

    String name = request.getParameter("name");

    List<VerificationTeam> teamList = groupFacade.findWithControlsAndUsers(name);

    String selectionMessage = null;

    if (name != null) {
      selectionMessage = "Team \"" + name + "\"";
    }

    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("teamList", teamList);

    request
        .getRequestDispatcher("/WEB-INF/views/inventory/verifiers.jsp")
        .forward(request, response);
  }
}
