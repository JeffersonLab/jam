package org.jlab.jam.presentation.controller.ajax;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.smoothness.presentation.util.ParamBuilder;
import org.jlab.smoothness.presentation.util.ServletUtil;

/**
 * Same Origin Policy prevents using the SRM app in a different environment to service searches, so
 * we proxy the requests in-app. In production when both JAM and SRM are actually on the same
 * origin, this isn't necessary, but it's easier to just keep consist between production and
 * development/testing environments by always using an in-app proxy.
 *
 * @author ryans
 */
@WebServlet(
    name = "ComponentSearch",
    urlPatterns = {"/data/components"})
public class ComponentSearch extends HttpServlet {

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

    String q = request.getParameter("q");

    String COMPONENT_QUERY_URL = "https://ace.jlab.org/srm/data/components";

    ParamBuilder builder = new ParamBuilder();
    builder.add("q", q);
    builder.add("application_id", "1");

    String queryString = ServletUtil.buildQueryString(builder.getParams(), "UTF-8");

    String uriStr = COMPONENT_QUERY_URL + queryString;

    try {
      HttpRequest proxyRequest =
          HttpRequest.newBuilder(new URI(uriStr)).header("Accept", "application/json").build();

      CompletableFuture<HttpResponse<String>> future =
          HttpClient.newHttpClient().sendAsync(proxyRequest, HttpResponse.BodyHandlers.ofString());

      HttpResponse<String> proxyResponse = future.get();

      response.getWriter().println(proxyResponse.body());
    } catch (URISyntaxException | InterruptedException | ExecutionException e) {
      throw new ServletException("Unable to query myquery", e);
    }
  }
}
