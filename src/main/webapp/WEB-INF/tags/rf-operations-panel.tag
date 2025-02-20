<%@tag description="RF Operations Table Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="jam" uri="http://jlab.org/jam/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@attribute name="rfList" required="true" type="java.util.List"%>
<%@attribute name="isHistory" required="true" type="java.lang.Boolean"%>
<table class="destinations-table data-table stripped-table">
    <thead>
        <tr>
            <th rowspan="2" class="destination-header">RF Segment</th>
                <c:if test="${not isHistory}">
                <th rowspan="2" class="approval-header">Auth</th>
                </c:if>
            <th colspan="3">Director's Permission</th>
                <c:if test="${not isHistory}">
                <th rowspan="2" class="cc-status-header">Credited Controls Status</th>
                </c:if>
        </tr>
        <tr>
            <th>High Power RF</th>
            <th>Comment</th>
            <th class="expiration-header">Expiration</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${rfList}" var="segment">
            <c:set var="segmentAuthorization" value="${segmentAuthorizationMap[segment.getRFSegmentId()]}"/>
            <tr>
                <td>
                    <a data-dialog-title="${segment.name} Verification" class="dialog-ready" href="${pageContext.request.contextPath}/verifications/segment?segmentId=${segment.getRFSegmentId()}&notEditable=Y"><c:out value="${segment.name}"/></a>
                    <input type="hidden" name="rfSegmentId[]" value="${segment.getRFSegmentId()}"/>
                </td>
                    <c:if test="${not isHistory}">
                    <td class="icon-cell">
                        <c:choose>
                            <c:when test="${(segment.verification.verificationStatusId eq 1 or segment.verification.verificationStatusId eq 50) and segmentAuthorization.isHighPowerRf()}">
                                <span title="Approved" class="small-icon verified-icon"></span>
                            </c:when>
                            <c:otherwise>
                                <span title="Not Approved" class="small-icon not-verified-icon"></span>
                            </c:otherwise>
                        </c:choose>                                         
                    </td>
                </c:if>
                <td>
                    <c:set var="selectedHighPowerRf" value="${segmentAuthorization.highPowerRf ? 'Yes' : 'No'}"/>
                    <div class="readonly-field"><c:out value="${selectedHighPowerRf}"/></div>
                    <div class="editable-field">
                        <select name="mode[]" class="mode-select">
                            <option${'No' eq selectedHighPowerRf ? ' selected="selected"' : ''}>No</option>
                            <option${'Yes' eq selectedHighPowerRf ? ' selected="selected"' : ''}>Yes</option>
                        </select>
                    </div>
                </td>
                <td class="${(not isHistory) && (not (selectedHighPowerRf eq 'No')) && (segment.verification.verificationStatusId eq 50) ? 'provisional-comments' : ''}">
                    <c:set var="selectedComment" value="${segmentAuthorization.comments eq null ? '' : segmentAuthorization.comments}"/>
                    <span class="readonly-field">
                        <c:out value="${selectedComment}"/>
                    </span>
                    <span class="editable-field">
                        <textarea name="comment[]" class="comment-input" type="text"${selectedHighPowerRf eq 'No' ? ' readonly="readonly"' : ''}><c:out value="${selectedHighPowerRf eq 'No' ? '' : selectedComment}"/></textarea>
                    </span>
                </td>
                <td>
                    <fmt:formatDate var="selectedExpiration" value="${segmentAuthorization.expirationDate}" pattern="${s:getFriendlyDateTimePattern()}"/>
                    <span class="readonly-field">
                        <c:out value="${selectedExpiration}"/>
                        <c:if test="${selectedHighPowerRf ne 'No'}">
                            <span class="expiring-soon" style="<c:out value="${jam:isExpiringSoon(segmentAuthorization.expirationDate) ? 'display: inline-block;' : 'display: none;'}"/>">(Expiring Soon)</span>
                        </c:if>
                    </span>
                    <span class="editable-field">
                        <input name="expiration[]" type="text" class="expiration-input date-time-field" autocomplete="off" placeholder="${s:getFriendlyDateTimePlaceholder()}" value="${selectedHighPowerRf eq 'No' ? '' : selectedExpiration}"${selectedHighPowerRf eq 'No' ? ' readonly="readonly"' : ''}/>
                    </span>
                </td>
                <c:if test="${not isHistory}">
                    <td class="icon-cell">
                            <c:choose>
                                <c:when test="${segment.verification.verificationStatusId eq 1}">
                                    <span title="Verified" class="small-icon verified-icon"></span>
                                </c:when>
                                <c:when test="${segment.verification.verificationStatusId eq 50}">
                                    <span title="Provisionally Verified" class="small-icon provisional-icon"></span>
                                </c:when>
                                <c:otherwise>
                                    <span title="Not Verified" class="small-icon not-verified-icon"></span>
                                </c:otherwise>
                            </c:choose>
                        <c:if test="${segment.verification.verificationStatusId ne 100}">
                            <span class="expiring-soon" style="<c:out value="${jam:isExpiringSoon(segment.verification.expirationDate) ? 'display: inline-block;' : 'display: none;'}"/>">(Expiring Soon)</span>
                        </c:if>
                    </td>
                </c:if>
            </tr>
        </c:forEach> 
    </tbody>
</table>
<h3>Notes</h3>
<div class="notes-field">
<span class="auth-notes-span readonly-field"><c:out value="${fn:length(rfAuthorization.comments) == 0 ? 'None' : rfAuthorization.comments}"/></span>
    <span class="editable-field">
                <textarea id="rf-comments" name="comments"><c:out value="${rfAuthorization.comments}"/></textarea>
            </span>
</div>
<h3 class="readonly-field">Digital Signature</h3>
<div class="footer-panel">
    <div class="footer-item signature-panel">
        <c:choose>
            <c:when test="${rfAuthorization ne null}">
                <div class="readonly-field">Authorized by ${s:formatUsername(rfAuthorization.authorizedBy)} on <fmt:formatDate value="${rfAuthorization.authorizationDate}" pattern="${s:getFriendlyDateTimePattern()}"/></div>
            </c:when>
            <c:otherwise>
                <div class="readonly-field">None</div>
            </c:otherwise>
        </c:choose>
    </div>
    <div class="footer-item logentry-panel">
        <c:if test="${not empty rfAuthorization.logentryUrl && param.print ne 'Y'}">
            <a href="${rfAuthorization.logentryUrl}">Log Entry</a>
        </c:if>
    </div>
</div>