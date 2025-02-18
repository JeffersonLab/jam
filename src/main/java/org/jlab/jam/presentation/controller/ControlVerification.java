package org.jlab.jam.presentation.controller;

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
import org.jlab.jam.business.session.AbstractFacade.OrderDirective;
import org.jlab.jam.business.session.BeamControlVerificationFacade;
import org.jlab.jam.business.session.CreditedControlFacade;
import org.jlab.jam.business.session.FacilityFacade;
import org.jlab.jam.persistence.entity.*;
import org.jlab.jam.persistence.view.FacilityControlVerification;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "ControlVerification",
    urlPatterns = {"/verifications/control"})
public class ControlVerification extends HttpServlet {

  @EJB CreditedControlFacade ccFacade;
  @EJB BeamControlVerificationFacade verificationFacade;
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

    BigInteger creditedControlId = ParamConverter.convertBigInteger(request, "creditedControlId");

    BigInteger facilityId = ParamConverter.convertBigInteger(request, "facilityId");

    Facility facility = null;

    if (facilityId != null) {
      facility = facilityFacade.find(facilityId);

      if (facility == null) {
        throw new ServletException("Facility not found with ID: " + facilityId);
      }
    }

    List<BeamControlVerification> expiredList = null;
    List<BeamControlVerification> expiringList = null;
    CreditedControl creditedControl = null;
    boolean adminOrLeader = false;

    if (creditedControlId != null) {
      creditedControl = ccFacade.findWithVerificationListTrio(creditedControlId);

      if (creditedControl != null) {
        removeInactiveVerificationsAndFilter(creditedControl, facility);

        String username = request.getRemoteUser();

        if (username != null) {
          String[] tokens = username.split(":");
          if (tokens.length > 1) {
            username = tokens[2];
          }
        }

        adminOrLeader =
            ccFacade.isAdminOrGroupLeader(
                username, creditedControl.getVerificationTeam().getVerificationTeamId());
      }
    } else {
      expiredList = verificationFacade.checkForExpired();
      expiringList = verificationFacade.checkForUpcomingVerificationExpirations();
    }

    List<CreditedControl> ccList = ccFacade.findAll(new OrderDirective("weight"));

    List<Facility> facilityList = facilityFacade.findAll(new OrderDirective("weight"));

    String selectionMessage = VerificationsController.getSelectionMessage(facility, null);

    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("adminOrLeader", adminOrLeader);
    request.setAttribute("creditedControl", creditedControl);
    request.setAttribute("ccList", ccList);
    request.setAttribute("facilityList", facilityList);
    request.setAttribute("expiredList", expiredList);
    request.setAttribute("expiringList", expiringList);

    request
        .getRequestDispatcher("/WEB-INF/views/control-verification.jsp")
        .forward(request, response);
  }

  private void removeInactiveVerificationsAndFilter(CreditedControl control, Facility facility) {
    if (control.getBeamControlVerificationList() != null) {
      List<BeamControlVerification> beamVerificationList = new ArrayList<>();
      for (BeamControlVerification bc : control.getBeamControlVerificationList()) {
        if (bc.getBeamDestination().isActive()) {
          boolean add = true;

          if (facility != null) {
            if (!bc.getBeamDestination().getFacility().equals(facility)) {
              add = false;
            }
          }

          if (add) {
            beamVerificationList.add(bc);
          }
        }
      }
      control.setBeamControlVerificationList(beamVerificationList);
    }

    if (control.getRFControlVerificationList() != null) {
      List<RFControlVerification> rfVerificationList = new ArrayList<>();
      for (RFControlVerification bc : control.getRFControlVerificationList()) {
        if (bc.getRFSegment().isActive()) {
          boolean add = true;

          if (facility != null) {
            if (!bc.getRFSegment().getFacility().equals(facility)) {
              add = false;
            }
          }

          if (add) {
            rfVerificationList.add(bc);
          }
        }
      }
      control.setRFControlVerificationList(rfVerificationList);
    }

    if (facility != null) {
      for (FacilityControlVerification facilityControlVerification :
          new ArrayList<>(control.getFacilityControlVerificationList())) {
        if (!facilityControlVerification
            .getFacilityControlVerificationPK()
            .getFacility()
            .equals(facility)) {
          control.getFacilityControlVerificationList().remove(facilityControlVerification);
        }
      }
    }
  }
}
