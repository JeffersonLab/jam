package org.jlab.jam.presentation.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.jam.business.session.AbstractFacade.OrderDirective;
import org.jlab.jam.business.session.ControlVerificationFacade;
import org.jlab.jam.business.session.CreditedControlFacade;
import org.jlab.jam.persistence.entity.ControlVerification;
import org.jlab.jam.persistence.entity.CreditedControl;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "CreditedControls",
    urlPatterns = {"/credited-controls"})
public class CreditedControls extends HttpServlet {

  @EJB CreditedControlFacade ccFacade;
  @EJB ControlVerificationFacade verificationFacade;

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

    BigInteger creditedControlId = ParamConverter.convertBigInteger(request, "creditedControlId");

    List<ControlVerification> expiredList = null;
    List<ControlVerification> expiringList = null;
    CreditedControl creditedControl = null;
    boolean adminOrLeader = false;

    if (creditedControlId != null) {
      creditedControl = ccFacade.findWithVerificationList(creditedControlId);

      if (creditedControl != null) {
        String username = request.getRemoteUser();

        if (username != null) {
          String[] tokens = username.split(":");
          if (tokens.length > 1) {
            username = tokens[2];
          }
        }

        adminOrLeader =
            ccFacade.isAdminOrGroupLeader(username, creditedControl.getGroup().getWorkgroupId());
      }
    } else {
      expiredList = verificationFacade.checkForExpired();
      expiringList = verificationFacade.checkForUpcomingVerificationExpirations();
    }

    List<CreditedControl> ccList = ccFacade.findAll(new OrderDirective("weight"));

    request.setAttribute("adminOrLeader", adminOrLeader);
    request.setAttribute("creditedControl", creditedControl);
    request.setAttribute("ccList", ccList);
    request.setAttribute("expiredList", expiredList);
    request.setAttribute("expiringList", expiringList);

    request.getRequestDispatcher("WEB-INF/views/credited-controls.jsp").forward(request, response);
  }
}
