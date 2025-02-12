package org.jlab.jam.presentation.controller.ajax;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import org.jlab.jam.business.session.WatcherFacade;
import org.jlab.jam.persistence.enumeration.OperationsType;
import org.jlab.jam.presentation.controller.inventory.AuthorizersController;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.ExceptionUtil;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "AddWatcher",
    urlPatterns = {"/ajax/add-watcher"})
public class AddWatcher extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(AddWatcher.class.getName());

  @EJB WatcherFacade watcherFacade;

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String errorReason = null;

    try {
      BigInteger facilityId = ParamConverter.convertBigInteger(request, "facilityId");
      String username = request.getParameter("username");
      OperationsType type = AuthorizersController.convertOperationsType(request, "type");

      watcherFacade.addWatcher(facilityId, type, username);
    } catch (UserFriendlyException e) {
      errorReason = e.getUserMessage();
    } catch (EJBException e) {
      Throwable root = ExceptionUtil.getRootCause(e);
      if (e instanceof EJBAccessException || root instanceof EJBAccessException) {
        LOGGER.log(Level.WARNING, "Not authorized", e);
        errorReason = "Not authorized";
      } else if (root instanceof ConstraintViolationException) {
        LOGGER.log(Level.WARNING, "Invalid input data", e);
        errorReason = root.getMessage();
      } else {
        LOGGER.log(Level.WARNING, "Transaction rolled back", e);
        errorReason =
            "Unknown Exception of type: " + e.getCausedByException().getClass().getSimpleName();
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unable to add watcher", e);
      errorReason = e.getClass().getSimpleName();
    }

    String stat = "ok";

    if (errorReason != null) {
      stat = "fail";
    }

    response.setContentType("application/json");

    OutputStream out = response.getOutputStream();

    try (JsonGenerator gen = Json.createGenerator(out)) {
      gen.writeStartObject().write("stat", stat); // This is unnecessary - if 200 OK then it worked
      if (errorReason != null) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        gen.write("error", errorReason);
      }
      gen.writeEnd();
    }
  }
}
