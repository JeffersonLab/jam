<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<c:set var="title" value="Facility Operations Authorizers"/>
<t:inventory-page title="${title}">
    <jsp:attribute name="stylesheets">
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/authorizers.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <section>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
            <div class="message-box"><c:out value="${selectionMessage}"/></div>
            <div id="chart-wrap" class="chart-wrap-backdrop">
                <c:set var="readonly" value="${!pageContext.request.isUserInRole('jam-admin')}"/>
                <c:if test="${not readonly}">
                    <s:editable-row-table-controls excludeEdit="true">
                    </s:editable-row-table-controls>
                </c:if>
                <table class="data-table stripped-table ${readonly ? '' : 'uniselect-table editable-row-table'}">
                    <thead>
                        <tr>
                            <th>Facility</th>
                            <th>Operations Type</th>
                            <th>Name</th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${authorizerList}" var="authorizer">
                        <tr data-username="${fn:escapeXml(authorizer.authorizerPK.username)}" data-facility-id="${authorizer.authorizerPK.facility.facilityId}">
                            <td><c:out value="${authorizer.authorizerPK.facility.name}"/></td>
                            <td><c:out value="${authorizer.authorizerPK.operationsType}"/></td>
                            <td><c:out value="${s:formatUsername(authorizer.authorizerPK.username)}"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </section>
        <s:editable-row-table-dialog>
            <form id="row-form">
                <ul class="key-value-list">
                    <li>
                        <div class="li-key">
                            <label for="row-facility">Facility</label>
                        </div>
                        <div class="li-value">
                            <select id="row-facility" required="required">
                                <option value="">&nbsp;</option>
                                <c:forEach items="${facilityList}" var="facility">
                                    <option value="${facility.facilityId}">
                                        <c:out value="${facility.name}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="row-type">Operations Type</label>
                        </div>
                        <div class="li-value">
                            <select id="row-type" required="required">
                                <option value="">&nbsp;</option>
                                <option value="RF">RF</option>
                                <option value="BEAM">BEAM</option>
                            </select>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="row-username">Username</label>
                        </div>
                        <div class="li-value">
                            <input type="text" required="required" id="row-username"/>
                        </div>
                    </li>
                </ul>
            </form>
        </s:editable-row-table-dialog>
    </jsp:body>         
</t:inventory-page>