package org.jlab.bam.presentation.controller;

import java.io.IOException;
import java.math.BigInteger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.bam.business.session.WorkgroupFacade;
import org.jlab.bam.persistence.entity.Workgroup;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 *
 * @author ryans
 */
@WebServlet(name = "GroupInformation", urlPatterns = {"/group-information"})
public class GroupInformation extends HttpServlet {

    @EJB
    WorkgroupFacade groupFacade;

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        BigInteger groupId = ParamConverter.convertBigInteger(request, "groupId");

        Workgroup group = null;
        
        if (groupId != null) {
            group = groupFacade.find(groupId);
        }

        request.setAttribute("group", group);

        request.getRequestDispatcher("WEB-INF/views/group-information.jsp").forward(request, response);
    }
}
