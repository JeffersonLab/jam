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
            <th colspan="3">Director's Status</th>
                <c:if test="${not isHistory}">
                <th rowspan="2" class="cc-status-header">Credited Controls Status</th>
                </c:if>
        </tr>
        <tr>
            <th>RF Mode</th>
            <th>Comment</th>
            <th class="expiration-header">Expiration</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${rfList}" var="segment">
            <c:set var="rfSegmentAuthorization" value="${rfAuthorizationMap[segment.getRFSegmentId()]}"/>
            <tr>
                <td><c:out value="${segment.name}"/></td>
                    <c:if test="${not isHistory}">
                    <td class="icon-cell">
                        <c:choose>
                            <c:when test="${(segment.verification.verificationStatusId eq 1 or segment.verification.verificationStatusId eq 50) and rfSegmentAuthorization.getRFMode() ne null and rfSegmentAuthorization.getRFMode() ne 'None'}">
                                <span title="Approved" class="small-icon verified-icon"></span>
                            </c:when>
                            <c:otherwise>
                                <span title="Not Approved" class="small-icon not-verified-icon"></span>
                            </c:otherwise>
                        </c:choose>                                         
                    </td>
                </c:if>
                <td>
                    <c:set var="selectedRFMode" value="${rfSegmentAuthorization.getRFMode() eq null ? 'None' : rfSegmentAuthorization.getRFMode()}"/>
                    <div class="readonly-field"><c:out value="${selectedRFMode}"/></div>
                    <div class="editable-field">
                        <select name="mode[]" class="mode-select">
                            <c:forEach items="${beamauth:rfModeList(facility.name, segment.name)}" var="mode">
                                <option${mode eq selectedRFMode ? ' selected="selected"' : ''}><c:out value="${mode}"/></option>
                            </c:forEach>
                        </select>
                        <c:out value="${mode}"/>
                    </div>
                </td>
                <td class="${(not isHistory) && (not (selectedBeamMode eq 'None')) && (segment.verification.verificationStatusId eq 50) ? 'provisional-comments' : ''}">
                    <c:set var="selectedComment" value="${rfSegmentAuthorization.comments eq null ? '' : rfSegmentAuthorization.comments}"/>
                    <span class="readonly-field">
                        <c:out value="${selectedComment}"/>
                    </span>
                    <span class="editable-field">
                        <textarea name="comment[]" class="comment-input" type="text"${selectedRfMode eq 'None' ? ' readonly="readonly"' : ''}><c:out value="${selectedBeamMode eq 'None' ? '' : selectedComment}"/></textarea>
                    </span>
                </td>
                <td>
                    <fmt:formatDate var="selectedExpiration" value="${rfSegmentAuthorization.expirationDate}" pattern="${s:getFriendlyDateTimePattern()}"/>
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
                    </td>
                </c:if>
            </tr>
        </c:forEach> 
    </tbody>
</table>