<%@tag description="Destination Table Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="beamauth" uri="http://jlab.org/beamauth/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@attribute name="beamList" required="true" type="java.util.List"%>
<%@attribute name="isHistory" required="true" type="java.lang.Boolean"%>
<table class="destinations-table data-table stripped-table">
    <thead>
        <tr>
            <th rowspan="2" class="destination-header">Beam Destination</th>
                <c:if test="${not isHistory}">
                <th rowspan="2" class="approval-header">Approval</th>
                </c:if>
            <th colspan="4">Director's Status</th>
                <c:if test="${not isHistory}">
                <th rowspan="2" class="cc-status-header">Credited Controls Status</th>
                </c:if>
        </tr>
        <tr>
            <th class="mode-header">Beam Mode</th>
            <th class="current-limit-header">Current Limit</th>
            <th>Comment</th>
            <th class="expiration-header">Expiration</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${beamList}" var="destination">
            <c:set var="beamDestinationAuthorization" value="${destinationAuthorizationMap[destination.beamDestinationId]}"/>
            <c:set var="units" value="${unitsMap[destination.beamDestinationId] ne null ? unitsMap[destination.beamDestinationId] : 'uA'}"/>
            <tr>
                <td><a data-dialog-title="${destination.name} Information" class="dialog-ready" href="${pageContext.request.contextPath}/beam-destination-information?beamDestinationId=${destination.beamDestinationId}"><c:out value="${destination.name}"/></a></td>
                    <c:if test="${not isHistory}">
                    <td class="icon-cell">
                        <c:choose>
                            <c:when test="${(destination.verification.verificationStatusId eq 1 or destination.verification.verificationStatusId eq 50) and beamDestinationAuthorization.beamMode ne null and beamDestinationAuthorization.beamMode ne 'None'}">
                                <span title="Approved" class="small-icon verified-icon"></span>
                            </c:when>
                            <c:otherwise>
                                <span title="Not Approved" class="small-icon not-verified-icon"></span>
                            </c:otherwise>
                        </c:choose>                                         
                    </td>
                </c:if>
                <td>
                    <c:set var="selectedBeamMode" value="${beamDestinationAuthorization.beamMode eq null ? 'None' : beamDestinationAuthorization.beamMode}"/>
                    <div class="readonly-field"><c:out value="${selectedBeamMode}"/></div>
                    <div class="editable-field">
                        <select name="mode[]" class="mode-select">
                            <c:forEach items="${beamauth:beamModeList(facility.name, destination.name)}" var="beamMode">
                                <option${beamMode eq selectedBeamMode ? ' selected="selected"' : ''}><c:out value="${beamMode}"/></option>
                            </c:forEach>
                        </select>
                        <c:out value="${beamMode}"/>
                    </div>
                </td>
                <td>
                    <c:set var="selectedLimit" value="${beamDestinationAuthorization.cwLimit eq null ? '' : beamDestinationAuthorization.cwLimit}"/>
                    <span class="readonly-field">
                        <c:choose>
                            <c:when test="${fn:contains(destination.name, 'RF Operations')}">
                                N/A
                            </c:when>
                            <c:when test="${selectedLimit ne ''}">
                                <fmt:formatNumber value="${selectedLimit}"/>
                                <c:out value="${units}"/>
                            </c:when>
                            <c:otherwise>
                                <c:if test="${selectedBeamMode ne 'None'}">
                                    <span class="power-limited">Dump Power Limited</span>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                    </span>
                    <span class="editable-field">
                        <input name="limit[]" class="limit-input" type="text" value="${selectedBeamMode eq 'None' ? '' : fn:escapeXml(selectedLimit)}"${selectedBeamMode eq 'None' || selectedBeamMode eq 'RF Only' ? ' readonly="readonly"' : ''}/>
                        <c:out value="${units}"/>
                    </span>
                    <input type="hidden" name="beamDestinationId[]" value="${destination.beamDestinationId}"/>
                </td>
                <td class="${(not isHistory) && (not (selectedBeamMode eq 'None')) && (destination.verification.verificationStatusId eq 50) ? 'provisional-comments' : ''}">
                    <c:set var="selectedComment" value="${beamDestinationAuthorization.comments eq null ? '' : beamDestinationAuthorization.comments}"/>
                    <span class="readonly-field">
                        <c:out value="${selectedComment}"/>
                    </span>
                    <span class="editable-field">
                        <textarea name="comment[]" class="comment-input" type="text"${selectedBeamMode eq 'None' ? ' readonly="readonly"' : ''}><c:out value="${selectedBeamMode eq 'None' ? '' : selectedComment}"/></textarea>
                    </span>
                </td>
                <td>
                    <fmt:formatDate var="selectedExpiration" value="${beamDestinationAuthorization.expirationDate}" pattern="${s:getFriendlyDateTimePattern()}"/>
                    <span class="readonly-field">
                        <c:out value="${selectedExpiration}"/>
                        <span class="expiring-soon" style="<c:out value="${beamDestinationAuthorization.expirationDate ne null and beamDestinationAuthorization.expirationDate.time > beamauth:now().time and beamDestinationAuthorization.expirationDate.time < beamauth:twoDaysFromNow().time ? 'display: block;' : 'display: none;'}"/>">(Expiring Soon)</span>
                    </span>
                    <span class="editable-field">
                        <input name="expiration[]" type="text" class="expiration-input date-time-field" autocomplete="off" placeholder="${s:getFriendlyDateTimePlaceholder()}" value="${selectedBeamMode eq 'None' ? '' : selectedExpiration}"${selectedBeamMode eq 'None' ? ' readonly="readonly"' : ''}/>
                    </span>
                </td>
                <c:if test="${not isHistory}">
                    <td class="icon-cell">
                        <a data-dialog-title="${destination.name} Information" class="dialog-ready" href="beam-destination-information?beamDestinationId=${destination.beamDestinationId}">
                            <c:choose>
                                <c:when test="${destination.verification.verificationStatusId eq 1}">
                                    <span title="Verified" class="small-icon verified-icon"></span>
                                </c:when>
                                <c:when test="${destination.verification.verificationStatusId eq 50}">
                                    <span title="Provisonally Verified" class="small-icon provisional-icon"></span>
                                </c:when>
                                <c:otherwise>
                                    <span title="Not Verified" class="small-icon not-verified-icon"></span>
                                </c:otherwise>
                            </c:choose>
                            <span class="expiring-soon" style="<c:out value="${destination.verification.expirationDate ne null and destination.verification.expirationDate.time > beamauth:now().time and destination.verification.expirationDate.time < beamauth:twoDaysFromNow().time ? 'display: block;' : 'display: none;'}"/>">(Expiring Soon)</span>                                    
                        </a>
                    </td>
                </c:if>
            </tr>
        </c:forEach> 
    </tbody>
</table>