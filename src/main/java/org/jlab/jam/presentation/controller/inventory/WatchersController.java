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
import org.jlab.jam.business.session.FacilityFacade;
import org.jlab.jam.business.session.WatcherFacade;
import org.jlab.jam.persistence.entity.Facility;
import org.jlab.jam.persistence.entity.Watcher;
import org.jlab.jam.persistence.enumeration.OperationsType;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "WatchersController",
    urlPatterns = {"/inventory/watchers"})
public class WatchersController extends HttpServlet {

  @EJB FacilityFacade facilityFacade;
  @EJB WatcherFacade watcherFacade;

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
    try {
      BigInteger facilityId = ParamConverter.convertBigInteger(request, "facilityId");
      String username = request.getParameter("username");
      OperationsType type = AuthorizersController.convertOperationsType(request, "type");

      Facility facility = null;

      if (facilityId != null) {
        facility = facilityFacade.find(facilityId);
      }

      List<Facility> facilityList =
          facilityFacade.findAll(new AbstractFacade.OrderDirective("weight"));
      List<Watcher> watcherList = watcherFacade.filterList(facility, type, username);

      String selectionMessage = createSelectionMessage(facility, type, username);

      request.setAttribute("selectionMessage", selectionMessage);
      request.setAttribute("facilityList", facilityList);
      request.setAttribute("watcherList", watcherList);

      request
          .getRequestDispatcher("/WEB-INF/views/inventory/watchers.jsp")
          .forward(request, response);
    } catch (UserFriendlyException e) {
      throw new ServletException(e);
    }
  }

  private String createSelectionMessage(Facility facility, OperationsType type, String username) {
    String selectionMessage = "All Watchers";

    List<String> filters = new ArrayList<>();

    if (facility != null) {
      filters.add("Facility \"" + facility.getName() + "\"");
    }

    if (type != null) {
      filters.add("Operations Type \"" + type + "\"");
    }

    if (username != null && !username.isEmpty()) {
      filters.add("Username \"" + username + "\"");
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
