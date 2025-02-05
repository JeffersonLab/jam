<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Credited Control Verifiers"/>
<t:inventory-page title="${title}">
    <jsp:attribute name="stylesheets">
    </jsp:attribute>
    <jsp:attribute name="scripts">
    </jsp:attribute>
    <jsp:body>
        <section>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
            <div class="message-box"><c:out value="${selectionMessage}"/></div>
            <div id="chart-wrap" class="chart-wrap-backdrop">
                <c:set var="readonly" value="${true}"/>
                <c:if test="${not readonly}">
                    <s:editable-row-table-controls>
                    </s:editable-row-table-controls>
                </c:if>
                <div class="dialog-content">
                    <table id="verifiers-table" class="data-table stripped-table ${readonly ? '' : 'uniselect-table editable-row-table'}">
                        <thead>
                        <tr>
                            <c:if test="${empty param.name}">
                                <th>Team Name</th>
                            </c:if>
                            <c:if test="${pageContext.request.isUserInRole('jam-admin')}">
                                <th>User Directory Role Name</th>
                            </c:if>
                            <th>Members</th>
                            <th>Credited Controls</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${teamList}" var="team">
                            <tr data-id="${team.verificationTeamId}">
                                <c:if test="${empty param.name}">
                                    <td><c:out value="${team.name}"/></td>
                                </c:if>
                                <c:if test="${pageContext.request.isUserInRole('jam-admin')}">
                                    <td><c:out value="${team.directoryRoleName}"/></td>
                                </c:if>
                                <td>
                                    <ul>
                                        <c:forEach items="${team.userList}" var="user">
                                            <li><c:out value="${s:formatUser(user)}"/></li>
                                        </c:forEach>
                                    </ul>
                                </td>
                                <td>
                                    <ul>
                                        <c:forEach items="${team.controlList}" var="control">
                                            <li><c:out value="${control.name}"/></li>
                                        </c:forEach>
                                    </ul>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
    </jsp:body>
</t:inventory-page>