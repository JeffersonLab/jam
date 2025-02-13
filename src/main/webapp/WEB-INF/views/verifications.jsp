<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="jam" uri="http://jlab.org/jam/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Verifications"/>
<t:page title="${title}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/verifications.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">          
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/verifications.js"></script>
    </jsp:attribute>
    <jsp:body>
        <section>
        <div class="top-right-box"><a id="expired-link" href="#">Expired</a> | <a id="expiring-link"
                                                                                 href="#">Expiring</a></div>
        <s:filter-flyout-widget ribbon="true" clearButton="true">
            <form id="filter-form" method="get" action="verifications">
                <div id="filter-form-panel">
                    <fieldset>
                        <legend>Filter</legend>
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key">
                                    <label for="facility-select">Facility</label>
                                </div>
                                <div class="li-value">
                                    <select id="facility-select" name="facilityId">
                                        <option value="">&nbsp;</option>
                                        <c:forEach items="${facilityList}" var="facility">
                                            <option value="${facility.facilityId}"${param.facilityId eq facility.facilityId ? ' selected="selected"' : ''}>
                                                <c:out value="${facility.name}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </li>
                            <li>
                                <div class="li-key">
                                    <label for="team-select">Team</label>
                                </div>
                                <div class="li-value">
                                    <select id="team-select" name="teamId">
                                        <option value="">&nbsp;</option>
                                        <c:forEach items="${teamList}" var="team">
                                            <option value="${team.verificationTeamId}"${param.teamId eq team.verificationTeamId ? ' selected="selected"' : ''}>
                                                <c:out value="${team.name}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </li>
                        </ul>
                    </fieldset>
                </div>
                <input id="filter-form-submit-button" type="submit" value="Apply"/>
            </form>
        </s:filter-flyout-widget>
        <h2 id="page-header-title"><c:out value="${title}"/></h2>
        <div class="message-box"><c:out value="${selectionMessage}"/></div>
        <div class="accordion">
            <h3>Credited Controls</h3>
            <div class="content">
                <c:choose>
                    <c:when test="${not empty ccList}">
                        <table id="credited-control-verifications-table" class="data-table">
                            <thead>
                                <tr>
                                    <th colspan="3">Facility Control Status with Expiration</th>
                                </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${ccList}" var="cc">
                                <tr>
                                    <td><c:out value="${cc.name}"/></td>
                                    <td>
                                        <ul>
                                            <c:forEach items="${cc.facilityControlVerificationList}" var="fv">
                                                <li>
                                                    <c:choose>
                                                        <c:when test="${fv.verificationStatusId eq 1}">
                                                            <span title="Verified"
                                                                  class="small-icon baseline-small-icon verified-icon"></span>
                                                        </c:when>
                                                        <c:when test="${fv.verificationStatusId eq 50}">
                                                            <span title="Verified"
                                                                  class="small-icon baseline-small-icon provisional-icon"></span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span title="Not Verified"
                                                                  class="small-icon baseline-small-icon not-verified-icon"></span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <c:out value="${fv.getFacilityControlVerificationPK().facility.name}"/>
                                                    <c:if test="${fv.verificationStatusId ne 100}">
                                                        <fmt:formatDate value="${fv.expirationDate}"
                                                                        pattern="${s:getFriendlyDateTimePattern()}"/>
                                                        <span class="expiring-soon"
                                                              style="<c:out value="${jam:isExpiringSoon(fv.expirationDate) ? 'display: inline-block;' : 'display: none;'}"/>">(Expiring Soon)</span>
                                                    </c:if>
                                                </li>
                                            </c:forEach>
                                        </ul>
                                    </td>
                                    <td>
                                        <form method="get"
                                              action="${pageContext.request.contextPath}/verifications/control">
                                            <input type="hidden" name="creditedControlId"
                                                   value="${cc.creditedControlId}"/>
                                            <button class="single-char-button" type="submit">&rarr;</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:when>
                    <c:otherwise>
                        None
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        <div class="accordion">
            <h3>RF Segments</h3>
            <div class="content">
                <c:choose>
                    <c:when test="${not empty segmentList}">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th colspan="3">Facility Segment Status with Expiration</th>
                                </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${segmentList}" var="segment">
                                <tr>
                                    <td>
                                        <c:out value="${segment.name}"/>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${segment.verification.verificationStatusId eq 1}">
                                                <span title="Verified"
                                                      class="small-icon baseline-small-icon verified-icon"></span>
                                            </c:when>
                                            <c:when test="${segment.verification.verificationStatusId eq 50}">
                                                <span title="Verified"
                                                      class="small-icon baseline-small-icon provisional-icon"></span>
                                            </c:when>
                                            <c:otherwise>
                                                <span title="Not Verified"
                                                      class="small-icon baseline-small-icon not-verified-icon"></span>
                                            </c:otherwise>
                                        </c:choose>
                                        <c:out value="${segment.facility.name}"/>
                                        <c:if test="${segment.verification.verificationStatusId ne 100}">
                                            <fmt:formatDate value="${segment.verification.expirationDate}"
                                                            pattern="${s:getFriendlyDateTimePattern()}"/>
                                            <span class="expiring-soon"
                                                  style="<c:out value="${jam:isExpiringSoon(segment.verification.expirationDate) ? 'display: inline-block;' : 'display: none;'}"/>">(Expiring Soon)</span>
                                        </c:if>
                                    </td>
                                    <td>
                                        <form method="get"
                                              action="${pageContext.request.contextPath}/verifications/segment">
                                            <input type="hidden" name="segmentId" value="${segment.getRFSegmentId()}"/>
                                            <button class="single-char-button" type="submit">&rarr;</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:when>
                    <c:otherwise>
                        None
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        <div class="accordion">
            <h3>Beam Destinations</h3>
            <div class="content">
                <c:choose>
                    <c:when test="${not empty destinationList}">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th colspan="3">Facility Destination Status with Expiration</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${destinationList}" var="destination">
                                <tr>
                                    <td>
                                        <c:out value="${destination.name}"/>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${destination.verification.verificationStatusId eq 1}">
                                                <span title="Verified"
                                                      class="small-icon baseline-small-icon verified-icon"></span>
                                            </c:when>
                                            <c:when test="${destination.verification.verificationStatusId eq 50}">
                                                <span title="Verified"
                                                      class="small-icon baseline-small-icon provisional-icon"></span>
                                            </c:when>
                                            <c:otherwise>
                                                <span title="Not Verified"
                                                      class="small-icon baseline-small-icon not-verified-icon"></span>
                                            </c:otherwise>
                                        </c:choose>
                                        <c:out value="${destination.facility.name}"/>
                                        <c:if test="${destination.verification.verificationStatusId ne 100}">
                                            <fmt:formatDate value="${destination.verification.expirationDate}"
                                                            pattern="${s:getFriendlyDateTimePattern()}"/>
                                            <span class="expiring-soon"
                                                  style="<c:out value="${jam:isExpiringSoon(destination.verification.expirationDate) ? 'display: inline-block;' : 'display: none;'}"/>">(Expiring Soon)</span>
                                        </c:if>
                                    </td>
                                    <td>
                                        <form method="get"
                                              action="${pageContext.request.contextPath}/verifications/destination">
                                            <input type="hidden" name="destinationId"
                                                   value="${destination.beamDestinationId}"/>
                                            <button class="single-char-button" type="submit">&rarr;</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:when>
                    <c:otherwise>
                        None
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        <div id="expired-dialog" class="dialog" title="Expired Controls">
            <c:choose>
                <c:when test="${fn:length(expiredList) > 0}">
                    <table class="data-table stripped-table">
                        <thead>
                        <tr>
                            <th>Name</th>
                            <th>Beam Destination</th>
                            <th>Expiration Date</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${expiredList}" var="verification">
                            <tr>
                                <td><c:out value="${verification.creditedControl.name}"/></td>
                                <td><c:out value="${verification.beamDestination.name}"/></td>
                                <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}"
                                                    value="${verification.expirationDate}"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <div>No expired controls</div>
                </c:otherwise>
            </c:choose>
        </div>
        <div id="expiring-dialog" class="dialog" title="Controls Expiring within Seven Days">
            <c:choose>
                <c:when test="${fn:length(expiringList) > 0}">
                    <table class="data-table stripped-table">
                        <thead>
                        <tr>
                            <th>Name</th>
                            <th>Beam Destination</th>
                            <th>Expiration Date</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${expiringList}" var="verification">
                            <tr>
                                <td><c:out value="${verification.creditedControl.name}"/></td>
                                <td><c:out value="${verification.beamDestination.name}"/></td>
                                <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}"
                                                    value="${verification.expirationDate}"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <div>No controls expiring within seven days</div>
                </c:otherwise>
            </c:choose>
        </div>
    </jsp:body>
</t:page>
