<%@tag description="Destination Table Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="s" uri="jlab.tags.smoothness" %>
<%@taglib prefix="jam" uri="jlab.tags.jam"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@attribute name="beamList" required="true" type="java.util.List"%>
<%@attribute name="isHistory" required="true" type="java.lang.Boolean"%>
<table class="destinations-table data-table stripped-table">
    <thead>
        <tr>
            <th rowspan="2" class="destination-header">Beam Destination</th>
                <c:if test="${not isHistory}">
                <th rowspan="2" class="approval-header">Auth</th>
                </c:if>
            <th colspan="4">Director's Permission</th>
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
            <tr class="${isHistory and beamDestinationAuthorization.changed ? 'changed' : ''}">
                <td><a class="dialog-opener" href="${pageContext.request.contextPath}/verifications/destination?destinationId=${destination.beamDestinationId}&notEditable=Y"><c:out value="${destination.name}"/></a></td>
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
                        <select name="mode[]" class="beam-mode-select">
                            <c:forEach items="${jam:beamModeList(facility.name, destination.name)}" var="beamMode">
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
                        <input name="limit[]" class="limit-input" type="text" value="${selectedBeamMode eq 'None' ? '' : fn:escapeXml(selectedLimit)}"${selectedBeamMode eq 'None' ? ' readonly="readonly"' : ''}/>
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
                        <c:if test="${selectedBeamMode ne 'None'}">
                            <span class="expiring-soon" style="<c:out value="${jam:isExpiringSoon(beamDestinationAuthorization.expirationDate) ? 'display: inline-block;' : 'display: none;'}"/>">(Expiring Soon)</span>
                        </c:if>
                    </span>
                    <span class="editable-field">
                        <input name="expiration[]" type="text" class="expiration-input date-time-field" autocomplete="off" placeholder="${s:getFriendlyDateTimePlaceholder()}" value="${selectedBeamMode eq 'None' ? '' : selectedExpiration}"${selectedBeamMode eq 'None' ? ' readonly="readonly"' : ''}/>
                    </span>
                </td>
                <c:if test="${not isHistory}">
                    <td class="icon-cell">
                        <a class="dialog-opener" href="${pageContext.request.contextPath}/verifications/destination?destinationId=${destination.beamDestinationId}&notEditable=Y&simple=Y">
                        <c:choose>
                            <c:when test="${destination.verification.verificationStatusId eq 1}">
                                <span title="Verified" class="small-icon verified-icon"></span>
                            </c:when>
                            <c:when test="${destination.verification.verificationStatusId eq 50}">
                                <span title="Provisionally Verified" class="small-icon provisional-icon"></span>
                            </c:when>
                            <c:otherwise>
                                <span title="Not Verified" class="small-icon not-verified-icon"></span>
                            </c:otherwise>
                        </c:choose>
                        </a>
                        <c:if test="${destination.verification.verificationStatusId ne 100}">
                            <span class="expiring-soon" style="<c:out value="${jam:isExpiringSoon(destination.verification.expirationDate) ? 'display: inline-block;' : 'display: none;'}"/>">(Expiring Soon)</span>
                        </c:if>
                    </td>
                </c:if>
            </tr>
        </c:forEach> 
    </tbody>
</table>
<h3>Change Notes</h3>
<div class="notes-field">
<span class="auth-notes-span readonly-field"><c:out value="${fn:length(beamAuthorization.comments) == 0 ? 'None' : beamAuthorization.comments}"/></span>
    <span class="editable-field">
        <textarea id="beam-comments" name="comments"></textarea>
    </span>
</div>
<h3 class="readonly-field">Digital Signature</h3>
<div class="footer-panel">
    <div class="footer-item signature-panel">
        <c:choose>
            <c:when test="${beamAuthorization ne null}">
                <div class="readonly-field">Authorized by ${s:formatUsername(beamAuthorization.authorizedBy)} on <fmt:formatDate value="${beamAuthorization.authorizationDate}" pattern="${s:getFriendlyDateTimePattern()}"/></div>
                <c:if test="${beamAuthorization.isAutomatedReduction()}">
                    <div class="reduction-note readonly-field">
                        <sup>†</sup> Automatically reduced on <fmt:formatDate value="${beamAuthorization.modifiedDate}" pattern="${s:getFriendlyDateTimePattern()}"/>
                    </div>
                </c:if>
            </c:when>
            <c:otherwise>
                <div class="readonly-field">None</div>
            </c:otherwise>
        </c:choose>
    </div>
    <div class="footer-item logentry-panel">
        <c:if test="${not empty beamAuthorization.logentryUrl && param.print ne 'Y'}">
            <a href="${beamAuthorization.logentryUrl}">Log Entry</a>
        </c:if>
    </div>
</div>
