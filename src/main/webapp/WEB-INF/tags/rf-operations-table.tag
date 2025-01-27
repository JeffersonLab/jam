<%@tag description="RF Operations Table Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="beamauth" uri="http://jlab.org/beamauth/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@attribute name="rfList" required="true" type="java.util.List"%>
<%@attribute name="isHistory" required="true" type="java.lang.Boolean"%>
<table class="destinations-table data-table stripped-table">
    <thead>
        <tr>
            <th rowspan="2" class="destination-header">Segment</th>
                <c:if test="${not isHistory}">
                <th rowspan="2" class="approval-header">Approval</th>
                </c:if>
            <th colspan="2">Director's Status</th>
                <c:if test="${not isHistory}">
                <th rowspan="2" class="cc-status-header">Credited Controls Status</th>
                </c:if>
        </tr>
        <tr>
            <th>Comment</th>
            <th class="expiration-header">Expiration</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${rfList}" var="segment">
            <c:set var="beamDestinationAuthorization" value="${destinationAuthorizationMap[segment.beamDestinationId]}"/>
            <c:set var="units" value="${unitsMap[segment.beamDestinationId] ne null ? unitsMap[segment.beamDestinationId] : 'uA'}"/>
            <tr>
                <td><a data-dialog-title="${segment.name} Information" class="dialog-ready" href="${pageContext.request.contextPath}/beam-destination-information?beamDestinationId=${segment.beamDestinationId}"><c:out value="${segment.name}"/></a></td>
                    <c:if test="${not isHistory}">
                    <td class="icon-cell">
                        <c:choose>
                            <c:when test="${(segment.verification.verificationStatusId eq 1 or segment.verification.verificationStatusId eq 50) and beamDestinationAuthorization.beamMode ne null and beamDestinationAuthorization.beamMode ne 'None'}">
                                <span title="Approved" class="small-icon verified-icon"></span>
                            </c:when>
                            <c:otherwise>
                                <span title="Not Approved" class="small-icon not-verified-icon"></span>
                            </c:otherwise>
                        </c:choose>                                         
                    </td>
                </c:if>
                <td class="${(not isHistory) && (not (selectedBeamMode eq 'None')) && (segment.verification.verificationStatusId eq 50) ? 'provisional-comments' : ''}">
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
                        <a data-dialog-title="${segment.name} Information" class="dialog-ready" href="beam-destination-information?beamDestinationId=${segment.beamDestinationId}">
                            <c:choose>
                                <c:when test="${segment.verification.verificationStatusId eq 1}">
                                    <span title="Verified" class="small-icon verified-icon"></span>
                                </c:when>
                                <c:when test="${segment.verification.verificationStatusId eq 50}">
                                    <span title="Provisonally Verified" class="small-icon provisional-icon"></span>
                                </c:when>
                                <c:otherwise>
                                    <span title="Not Verified" class="small-icon not-verified-icon"></span>
                                </c:otherwise>
                            </c:choose>
                            <span class="expiring-soon" style="<c:out value="${segment.verification.expirationDate ne null and segment.verification.expirationDate.time > beamauth:now().time and segment.verification.expirationDate.time < beamauth:twoDaysFromNow().time ? 'display: block;' : 'display: none;'}"/>">(Expiring Soon)</span>
                        </a>
                    </td>
                </c:if>
            </tr>
        </c:forEach> 
    </tbody>
</table>