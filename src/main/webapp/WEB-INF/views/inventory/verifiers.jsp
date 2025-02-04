<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
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
                <table class="data-table stripped-table ${readonly ? '' : 'uniselect-table editable-row-table'}">
                    <thead>
                        <tr>
                            <th>Team Name</th>
                            <c:if test="${pageContext.request.isUserInRole('jam-admin')}">
                                <th>User Directory Role Name</th>
                            </c:if>
                            <th>Members</th>
                            <th>Credited Controls</th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${teamList}" var="team">
                        <tr data-id="${team.workgroupId}">
                            <td><c:out value="${team.name}"/></td>
                            <c:if test="${pageContext.request.isUserInRole('jam-admin')}">
                                <td><c:out value="${team.leaderRoleName}"/></td>
                            </c:if>
                            <td>
                                <ul>
                                <c:forEach items="${team.leaders}" var="leader">
                                    <li><c:out value="${s:formatUser(leader)}"/></li>
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
        </section>
    </jsp:body>         
</t:inventory-page>