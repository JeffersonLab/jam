<%@tag description="Verification Panel Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="jam" uri="http://jlab.org/jam/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@attribute name="operationsType" required="true" type="java.lang.String"%>
<%@attribute name="operationsList" required="true" type="java.util.List"%>
<c:set var="operationsName" value="Beam Destination"/>
<c:set var="operationsId" value="beamControlVerificationId"/>
<c:set var="operationsEntity" value="beamDestination"/>
<c:set var="historyPathSuffix" value="destination-history?beamControlVerificationId"/>
<c:if test="${'rf' eq operationsType}">
    <c:set var="operationsName" value="RF Segment"/>
    <c:set var="operationsId" value="RFControlVerificationId"/>
    <c:set var="operationsEntity" value="RFSegment"/>
    <c:set var="historyPathSuffix" value="segment-history?rfControlVerificationId"/>
</c:if>
<div class="verification-panel ${operationsType}">
    <c:if test="${adminOrLeader && param.notEditable eq null}">
        <button type="button" class="edit-selected-button verify-button selected-row-action" disabled="disabled">Edit Verification</button>
    </c:if>
    <c:if test="${pageContext.request.isUserInRole('jam-admin') && param.notEditable eq null}">
        <button type="button" class="component-edit-button single-select-row-action" disabled="disabled">Edit Components</button>
    </c:if>
    <table class="verification-table data-table stripped-table${(adminOrLeader && param.notEditable eq null) ? ' multicheck-table editable-row-table' : ''}">
        <thead>
        <tr>
            <c:if test="${adminOrLeader && param.notEditable eq null}">
                <th class="select-header">
                    Select
                    <select class="check-select" name="check-select">
                        <option value="">&nbsp;</option>
                        <option value="all">All</option>
                        <option value="none">None</option>
                    </select>
                </th>
            </c:if>
            <th>Facility / <c:out value="${operationsName}"/></th>
            <th>Verified</th>
            <th>Components</th>
            <th>Comments</th>
            <th>Expiration Date</th>
            <th class="audit-header">Audit</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${operationsList}" var="verification">
            <tr data-control-verification-id="${verification[operationsId]}" data-verified-username="${verification.verifiedBy}" data-status-id="${verification.verificationStatusId}">
                <c:if test="${adminOrLeader && param.notEditable eq null}">
                    <td>
                        <input class="destination-checkbox" type="checkbox" name="destination-checkbox" value="${verification[operationsId]}"/>
                    </td>
                </c:if>
                <td><c:out value="${verification[operationsEntity].facility.name}"/> / <c:out value="${verification[operationsEntity].name}"/></td>
                <td class="verified-cell">
                    <div title="${verification.verificationStatusId eq 1 ? 'Verified' : (verification.verificationStatusId eq 50 ? 'Provisionally Verified' : 'Not Verified')}" class="small-icon baseline-small-icon ${verification.verificationStatusId eq 1 ? 'verified-icon' : (verification.verificationStatusId eq 50 ? 'provisional-icon' : 'not-verified-icon')}"></div>
                    <div class="verified-date"><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${verification.verificationDate}"/></div>
                    <div class="verified-by"><c:out value="${s:formatUsername(verification.verifiedBy)}"/></div>
                </td>
                <td>
                    <c:forEach items="${verification.componentList}" var="component">
                        <div class="component-status" data-id="${component.componentId}">
                            <c:choose>
                                <c:when test="${component.statusId eq 1}">
                                    <span class="small-icon baseline-small-icon verified-icon"></span>
                                </c:when>
                                <c:otherwise>
                                    <span class="small-icon baseline-small-icon not-verified-icon"></span>
                                </c:otherwise>
                            </c:choose>
                            <a href="${env['JAM_COMPONENT_DETAIL_URL']}${fn:escapeXml(component.name)}"><c:out value="${component.name}"/></a>
                        </div>
                    </c:forEach>
                </td>
                <td><c:out value="${verification.comments}"/></td>
                <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${verification.expirationDate}"/></td>
                <td><a data-dialog-title="Verification History" href="${pageContext.request.contextPath}/verifications/control/${historyPathSuffix}=${verification[operationsId]}" title="Click for verification history">History</a></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <c:if test="${adminOrLeader && param.notEditable eq null}">
        <div id="multi-instructions">Hold down the control (Ctrl) or shift key when clicking to select multiple.  Hold down the Command (⌘) key on Mac.</div>
    </c:if>
</div>