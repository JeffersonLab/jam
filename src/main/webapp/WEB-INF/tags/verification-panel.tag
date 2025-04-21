<%@tag description="Verification Panel Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="jam" uri="http://jlab.org/jam/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@attribute name="operationsType" required="true" type="java.lang.String"%>
<%@attribute name="operationsList" required="true" type="java.util.List"%>
<%@attribute name="groupByOperation" required="true" type="java.lang.Boolean"%>
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
<c:set var="rowKey" value="Facility / ${operationsName}"/>
<c:if test="${groupByOperation}">
    <c:set var="rowKey" value="Team / Control"/>
</c:if>
<div class="verification-panel ${operationsType} ${groupByOperation ? 'groupByOperation' : ''}">
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
            <th><c:out value="${rowKey}"/></th>
            <th>Verified</th>
            <th>Expiration Date</th>
            <c:if test="${param.simple ne 'Y'}">
                <th>Components</th>
                <th>Comments</th>
                <th class="audit-header">Audit</th>
            </c:if>
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
                <td>
                    <c:choose>
                        <c:when test="${groupByOperation}">
                            <c:url value="/verifications/control" var="url">
                                <c:param name="creditedControlId" value="${verification.creditedControl.creditedControlId}"/>
                                <c:param name="notEditable" value="1"/>
                            </c:url>
                            <c:out value="${verification.creditedControl.verificationTeam.name}"/> / <a href="${url}" class="dialog-opener"><c:out value="${verification.creditedControl.name}"/></a>
                        </c:when>
                        <c:otherwise>
                            <c:out value="${verification[operationsEntity].facility.name}"/> / <c:out value="${verification[operationsEntity].name}"/>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="verified-cell">
                    <div title="${verification.verificationStatusId eq 1 ? 'Verified' : (verification.verificationStatusId eq 50 ? 'Provisionally Verified' : 'Not Verified')}" class="small-icon baseline-small-icon ${verification.verificationStatusId eq 1 ? 'verified-icon' : (verification.verificationStatusId eq 50 ? 'provisional-icon' : 'not-verified-icon')}"></div>
                    <div class="verified-date"><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${verification.verificationDate}"/></div>
                    <div class="verified-by"><c:out value="${s:formatUsername(verification.verifiedBy)}"/></div>
                </td>
                <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${verification.expirationDate}"/></td>
                <c:if test="${param.simple ne 'Y'}">
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
                <td>
                    <div class="comments-div"><c:out value="${verification.comments}"/></div>
                    <c:if test="${not empty verification.externalUrl}">
                        <br/>
                        <div>
                            <a class="doc-anchor" href="${verification.externalUrl}">Documentation</a>
                        </div>
                    </c:if>
                </td>
                <td><a class="${groupByOperation ? 'dialog-opener' : 'partial-support'}" href="${pageContext.request.contextPath}/verifications/control/${historyPathSuffix}=${verification[operationsId]}" title="Click for verification history">History</a></td>
                </c:if>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <c:if test="${adminOrLeader && param.notEditable eq null}">
        <div id="multi-instructions">Hold down the control (Ctrl) or shift key when clicking to select multiple.  Hold down the Command (⌘) key on Mac.</div>
    </c:if>
</div>
<div id="verify-dialog" class="dialog" title="Edit Credited Control Verification">
    <section>
    <form>
        <ul class="key-value-list">
            <li>
                <div class="li-key"><span id="edit-dialog-verification-label">Beam Destinations</span>:</div>
                <div class="li-value">
                    <ul id="selected-verification-list">

                    </ul>
                    <span id="edit-dialog-verification-count"></span>
                </div>
            </li>
            <li>
                <div class="li-key">Status:</div>
                <div class="li-value">
                    <select id="verificationId" name="verificationId">
                        <option value="&nbsp;"> </option>
                        <option value="100">Not Verified</option>
                        <option value="1">Verified</option>
                    </select>
                </div>
            </li>
            <li>
                <div class="li-key">Verification Date:</div>
                <div class="li-value">
                    <input id="verificationDate" name="verificationDate" type="text" class="date-time-field nowable-field" placeholder="${s:getFriendlyDateTimePlaceholder()}"/>
                </div>
            </li>
            <li>
                <div class="li-key">Verified By:</div>
                <div class="li-value">
                    <input id="verifiedBy" name="verifiedBy" type="text" placeholder="username" class="username-autocomplete" maxlength="64"/>
                    <button class="me-button" type="button">Me</button>
                </div>
            </li>
            <li>
                <div class="li-key">Expiration Date:</div>
                <div class="li-value">
                    <input id="expirationDate" name="expirationDate" type="text" class="date-time-field" placeholder="${s:getFriendlyDateTimePlaceholder()}" autocomplete="off"/>
                </div>
            </li>
            <li>
                <div class="li-key">Comments:</div>
                <div class="li-value">
                    <textarea id="comments" name="comments"></textarea>
                </div>
            </li>
            <li>
                <div class="li-key">Documentation URL:</div>
                <div class="li-value">
                    <input type="text" id="link" name="link"/>
                </div>
            </li>
        </ul>
        <input type="hidden" id="creditedControlId" name="creditedControlId"/>
        <div class="dialog-button-panel">
            <span id="rows-differ-message">Note: One or more selected rows existing values differ</span>
            <button id="verifySaveButton" class="dialog-submit ajax-button" type="button">Save</button>
            <button class="dialog-close-button" type="button">Cancel</button>
        </div>
        <input type="hidden" id="verificationType" name="verificationType"/>
    </form>
    </section>
</div>
<div id="success-dialog" class="dialog" title="Verification Saved Successfully">
    <section>
    <span class="logentry-success">Verification contained downgrade so a new log entry was created: <a id="new-entry-url" href="#"></a></span>
    <div class="dialog-button-panel">
        <button class="dialog-close-button" type="button">OK</button>
    </div>
    </section>
</div>
<div id="component-edit-dialog" class="dialog" title="Components">
    <section>
    <div class="row">
        <div class="column">
            <fieldset>
                <legend>Add</legend>
                <input type="text" id="component" name="component" placeholder="search for name" autocomplete="off"/>
                <button id="add-component-button" type="button">Add</button>
            </fieldset>
        </div>
        <div class="column">
            <fieldset>
                <legend>Remove</legend>
                <select id="selected-component-list">
                </select>
                <button id="remove-component-button" type="button">Remove</button>
            </fieldset>
        </div>
    </div>
    <div class="dialog-button-panel">
        <input type="hidden" id="component-edit-verification-type" name="verificationType"/>
        <input type="hidden" id="component-edit-verification-id" name="verificationId"/>
        <button class="dialog-close-button" type="button">OK</button>
    </div>
    </section>
</div>