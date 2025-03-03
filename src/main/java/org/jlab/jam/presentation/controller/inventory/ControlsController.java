package org.jlab.jam.presentation.controller.inventory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.jam.business.session.AbstractFacade;
import org.jlab.jam.business.session.CreditedControlFacade;
import org.jlab.jam.business.session.VerificationTeamFacade;
import org.jlab.jam.persistence.entity.CreditedControl;
import org.jlab.jam.persistence.entity.VerificationTeam;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "ControlsController",
    urlPatterns = {"/inventory/controls"})
public class ControlsController extends HttpServlet {

  @EJB CreditedControlFacade controlFacade;
  @EJB VerificationTeamFacade teamFacade;

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

    BigInteger controlId = ParamConverter.convertBigInteger(request, "controlId");

    List<CreditedControl> controlList = null;
    CreditedControl selectedControl = null;

    if (controlId != null) {
      controlList = new ArrayList<CreditedControl>();

      selectedControl = controlFacade.find(controlId);

      if (selectedControl != null) {
        controlList.add(selectedControl);
      }
    } else {
      controlList = controlFacade.findAll(new AbstractFacade.OrderDirective("weight"));
    }

    String selectionMessage = null;

    if (selectedControl != null) {
      selectionMessage = "Control \"" + selectedControl.getName() + "\"";
    }

    List<VerificationTeam> teamList = teamFacade.findAll(new AbstractFacade.OrderDirective("name"));

    request.setAttribute("teamList", teamList);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("controlList", controlList);

    request
        .getRequestDispatcher("/WEB-INF/views/inventory/controls.jsp")
        .forward(request, response);
  }
}
