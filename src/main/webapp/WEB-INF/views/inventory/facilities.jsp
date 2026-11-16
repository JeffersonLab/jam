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
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/facilities.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <section>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
            <div class="message-box"><c:out value="${selectionMessage}"/></div>
            <div id="chart-wrap" class="chart-wrap-backdrop">
                <c:set var="readonly" value="${!pageContext.request.isUserInRole('jam-admin')}"/>
                <c:if test="${not readonly}">
                    <s:editable-row-table-controls excludeAdd="true" excludeDelete="true">
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
                        <tr data-id="${facility.facilityId}" data-manager="${fn:escapeXml(facility.managerUsername)}" data-logbooks="${fn:escapeXml(facility.logbooksCsv)}">
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
                            <span>Name</span>
                        </div>
                        <div class="li-value">
                            <span id="row-name"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="row-manager">Manager</label>
                        </div>
                        <div class="li-value">
                            <input type="text" required="required" id="row-manager" maxlength="32" autocomplete="off" placeholder="username"/>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="row-logbooks">Logbooks</label>
                        </div>
                        <div class="li-value">
                            <input type="text" id="row-logbooks" maxlength="64" autocomplete="off" placeholder="TLOG,ELOG"/>
                        </div>
                    </li>
                </ul>
            </form>
            </section>
        </s:editable-row-table-dialog>
    </jsp:body>         
</t:inventory-page>