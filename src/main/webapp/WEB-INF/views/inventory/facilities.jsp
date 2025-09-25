<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="s" uri="jlab.tags.smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<c:set var="title" value="Facilities"/>
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
                            <th>Name</th>
                            <th>Manager</th>
                            <th>Logbooks</th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${facilityList}" var="facility">
                        <tr data-id="${facility.facilityId}">
                            <td><c:out value="${facility.name}"/></td>
                            <td><c:out value="${s:formatUsername(facility.managerUsername)}"/></td>
                            <td><c:out value="${facility.logbooksCsv}"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </section>
        <s:editable-row-table-dialog>
            <section>
            <form id="row-form">
                <ul class="key-value-list">
                    <li>
                        <div class="li-key">
                            <label for="row-name">Name</label>
                        </div>
                        <div class="li-value">
                            <input type="text" required="required" id="row-name"/>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="row-team">Team</label>
                        </div>
                        <div class="li-value">
                            <select id="row-team" required="required">
                                <option value="">&nbsp;</option>
                                <c:forEach items="${teamList}" var="team">
                                    <option value="${team.teamId}">
                                        <c:out value="${team.name}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                    </li>
                </ul>
            </form>
            </section>
        </s:editable-row-table-dialog>
    </jsp:body>         
</t:inventory-page>