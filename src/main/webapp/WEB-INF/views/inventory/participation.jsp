<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="beamauth" uri="http://jlab.org/beamauth/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Control Participation"/>
<t:inventory-page title="${title}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/credited-controls.css"/>
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/control-participation.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">              
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/control-participation.js"></script>
    </jsp:attribute>
    <jsp:body>
        <div class="banner-breadbox">
            <ul>
                <li>
                    <form method="get" action="participation">
                        <label for="facility-select">Facility: </label>
                        <select id="facility-select" name="facilityId" class="change-submit">
                            <option value=""></option>
                            <c:forEach items="${facilityList}" var="facility">
                                <option value="${facility.facilityId}"${param.facilityId eq facility.facilityId ? ' selected="selected"' : ''}>
                                    <c:out value="${facility.name}"/></option>
                            </c:forEach>
                        </select>
                    </form>
                </li>
            </ul>
        </div>
        <section>
            <h2><c:out value="${title}${empty facility ? '' : ': '.concat(facility.name)}"/></h2>
            <c:choose>
                <c:when test="${not empty error}">
                    <c:out value="${error}"/>
                </c:when>
                <c:otherwise>
                    <div class="accordion">
                        <h3>RF Operations</h3>
                        <div class="content rf-content">
                            <c:choose>
                                <c:when test="${not empty segmentList}">
                                    <div class="participation-scroll-pane">
                                        <table class="data-table stripped-table fixed-table control-participation-table ${pageContext.request.isUserInRole('jam-admin') ? 'editable' : ''}">
                                            <thead>
                                            <tr>
                                                <th rowspan="2" class="control-header">Credited Control</th>
                                                <th colspan="${fn:length(segmentList)}">RF Segment</th>
                                            </tr>
                                            <tr>
                                                <c:forEach items="${segmentList}" var="segment">
                                                    <th class="destination-header">
                                                        <c:out value="${segment.name}"/>
                                                    </th>
                                                </c:forEach>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <c:forEach items="${ccList}" var="cc">
                                                <tr data-cc-id="${cc.creditedControlId}">
                                                    <th><a data-dialog-title="${fn:escapeXml(cc.name)} Information"
                                                           class="dialog-ready"
                                                           href="${pageContext.request.contextPath}/credited-controls?creditedControlId=${cc.creditedControlId}&amp;notEditable=1"><c:out
                                                            value="${cc.name}"/></a></th>
                                                    <c:forEach items="${segmentList}" var="segment">
                                                        <td data-segment-id="${segment.getRFSegmentId()}">
                                                            <c:if test="${cc.hasRFSegment(segment)}">
                                                                ✔
                                                            </c:if>
                                                        </td>
                                                    </c:forEach>
                                                </tr>
                                            </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    None
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div class="accordion">
                        <h3>Beam Operations</h3>
                        <div class="content beam-content">
                            <c:choose>
                                <c:when test="${not empty destinationList}">
                                    <div class="participation-scroll-pane">
                                        <table class="data-table stripped-table fixed-table control-participation-table ${pageContext.request.isUserInRole('jam-admin') ? 'editable' : ''}">
                                            <thead>
                                            <tr>
                                                <th rowspan="2" class="control-header">Credited Control</th>
                                                <th colspan="${fn:length(destinationList)}">Beam Destination</th>
                                            </tr>
                                            <tr>
                                                <c:forEach items="${destinationList}" var="destination">
                                                    <th class="destination-header">
                                                        <c:out value="${destination.name}"/>
                                                    </th>
                                                </c:forEach>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <c:forEach items="${ccList}" var="cc">
                                                <tr data-cc-id="${cc.creditedControlId}">
                                                    <th><a data-dialog-title="${fn:escapeXml(cc.name)} Information"
                                                           class="dialog-ready"
                                                           href="${pageContext.request.contextPath}/credited-controls?creditedControlId=${cc.creditedControlId}&amp;notEditable=1"><c:out
                                                            value="${cc.name}"/></a></th>
                                                    <c:forEach items="${destinationList}" var="destination">
                                                        <td data-destination-id="${destination.beamDestinationId}">
                                                            <c:if test="${cc.hasBeamDestination(destination)}">
                                                                ✔
                                                            </c:if>
                                                        </td>
                                                    </c:forEach>
                                                </tr>
                                            </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    None
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </jsp:body>
</t:inventory-page>
