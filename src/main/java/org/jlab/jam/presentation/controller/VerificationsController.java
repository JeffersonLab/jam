package org.jlab.jam.presentation.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.jam.business.session.*;
import org.jlab.jam.business.session.AbstractFacade.OrderDirective;
import org.jlab.jam.persistence.entity.*;
import org.jlab.jam.persistence.view.FacilityUpcomingExpiration;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "VerificationsController",
    urlPatterns = {"/verifications"})
public class VerificationsController extends HttpServlet {

  @EJB CreditedControlFacade ccFacade;
  @EJB BeamControlVerificationFacade verificationFacade;
  @EJB RFSegmentFacade segmentFacade;
  @EJB BeamDestinationFacade destinationFacade;
  @EJB FacilityFacade facilityFacade;
  @EJB VerificationTeamFacade verificationTeamFacade;
  @EJB ExpirationManager expirationManager;

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

    BigInteger facilityId = ParamConverter.convertBigInteger(request, "facilityId");
    BigInteger teamId = ParamConverter.convertBigInteger(request, "teamId");

    Facility facility = null;

    if (facilityId != null) {
      facility = facilityFacade.find(facilityId);

      if (facility == null) {
        throw new ServletException("Facility not found with ID: " + facilityId);
      }
    }

    VerificationTeam team = null;

    if (teamId != null) {
      team = verificationTeamFacade.find(teamId);

      if (team == null) {
        throw new ServletException("Team not found with ID: " + teamId);
      }
    }

    Map<Facility, FacilityUpcomingExpiration> upcomingExpirationMap =
        expirationManager.getUpcomingExpirationMap(false);

    boolean adminOrLeader = false;

    List<CreditedControl> ccList = ccFacade.findWithFacilityVerification(facility, team);

    List<RFSegment> segmentList = segmentFacade.filterList(true, facility, team);
    List<BeamDestination> destinationList = destinationFacade.filterList(true, facility, team);

    List<Facility> facilityList = facilityFacade.findAll(new OrderDirective("weight"));
    List<VerificationTeam> teamList = verificationTeamFacade.findAll(new OrderDirective("name"));

    String selectionMessage = getSelectionMessage(facility, team);

    request.setAttribute("facility", facility);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("facilityList", facilityList);
    request.setAttribute("teamList", teamList);
    request.setAttribute("segmentList", segmentList);
    request.setAttribute("destinationList", destinationList);
    request.setAttribute("adminOrLeader", adminOrLeader);
    request.setAttribute("ccList", ccList);
    request.setAttribute("upcomingExpirationMap", upcomingExpirationMap);

    request.getRequestDispatcher("WEB-INF/views/verifications.jsp").forward(request, response);
  }

  public static String getSelectionMessage(Facility facility, VerificationTeam team) {
    String selectionMessage = "All Verifications";

    List<String> filters = new ArrayList<>();

    if (facility != null) {
      filters.add("Facility \"" + facility.getName() + "\"");
    }

    if (team != null) {
      filters.add("Team \"" + team.getName() + "\"");
    }

    if (!filters.isEmpty()) {
      selectionMessage = filters.get(0);

      for (int i = 1; i < filters.size(); i++) {
        String filter = filters.get(i);
        selectionMessage += " and " + filter;
      }
    }

    return selectionMessage;
  }
}
