<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="jam" uri="http://jlab.org/jam/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<c:set var="title" value="Segment Verification"/>
<t:page title="${title}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/control-verification.css"/>
        <style type="text/css">
            .dialog-content {
                padding-bottom: 1em;
            }
            .dialog-content button {
                font-size: 14px;
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript">
            jlab = jlab || {};
            jlab.verificationType = 'Control-Group';
        </script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/control-verification.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <c:if test="${segment ne null}">
            <div class="banner-breadbox">
                <ul>
                    <li>
                        <a href="${pageContext.request.contextPath}/verifications">Verifications</a>
                    </li>
                    <li>
                        <form method="get" action="segment">
                            <select name="segmentId" class="change-submit">
                                <c:forEach items="${segmentList}" var="segment">
                                    <option value="${segment.getRFSegmentId()}"${param.segmentId eq segment.getRFSegmentId() ? ' selected="selected"' : ''}><c:out value="${segment.name}"/></option>
                                </c:forEach>
                            </select>
                        </form>
                    </li>
                </ul>
            </div>
        </c:if>
        <section>
            <h2><c:out value="${title}${empty segment ? '' : ': '.concat(segment.name)}"/></h2>
            <c:choose>
                <c:when test="${segment ne null}">
                    <div class="dialog-content">
                        <h3>
                            <c:choose>
                                <c:when test="${segment.verification.verificationStatusId eq 1}">
                                    <span title="Verified" class="small-icon baseline-small-icon verified-icon"></span>
                                </c:when>
                                <c:when test="${segment.verification.verificationStatusId eq 50}">
                                    <span title="Verified" class="small-icon baseline-small-icon provisional-icon"></span>
                                </c:when>
                                <c:otherwise>
                                    <span title="Not Verified" class="small-icon baseline-small-icon not-verified-icon"></span>
                                </c:otherwise>
                            </c:choose>
                            Credited Control Verifications
                            <c:if test="${segment.verification.verificationStatusId ne 100}">
                                <fmt:formatDate value="${segment.verification.expirationDate}" pattern="${s:getFriendlyDateTimePattern()}"/>
                                <span class="expiring-soon" style="<c:out value="${jam:isExpiringSoon(segment.verification.expirationDate) ? 'display: inline-block;' : 'display: none;'}"/>">(Expiring Soon)</span>
                            </c:if>
                        </h3>
                        <c:choose>
                            <c:when test="${fn:length(segment.getRFControlVerificationList()) < 1}">
                                <div class="message-box">None</div>
                            </c:when>
                            <c:otherwise>
                                <c:if test="${adminOrLeader && param.notEditable eq null}">
                                    <button id="edit-selected-button" type="button" class="verify-button selected-row-action" disabled="disabled">Edit Selected</button>
                                </c:if>
                                <table id="verification-table" class="data-table stripped-table${(adminOrLeader && param.notEditable eq null) ? ' multicheck-table editable-row-table' : ''}">
                                    <thead>
                                        <tr>
                                            <c:if test="${adminOrLeader && param.notEditable eq null}">
                                                <th>
                                                    Select
                                                    <select id="check-select" name="check-select">
                                                        <option value="">&nbsp;</option>
                                                        <option value="all">All</option>
                                                        <option value="none">None</option>
                                                    </select>
                                                </th>
                                            </c:if>
                                            <th>Credited Control and Group</th>
                                            <th>Verified</th>
                                            <th>Verified Date</th>
                                            <th>Verified By</th>
                                            <th>Comments</th>
                                            <th>Expiration Date</th>
                                            <th>Audit</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${segment.getRFControlVerificationList()}" var="verification">
                                            <tr data-control-verification-id="${verification.getRFControlVerificationId()}" data-verified-username="${verification.verifiedBy}" data-status-id="${verification.verificationStatusId}">
                                                <c:if test="${adminOrLeader && param.notEditable eq null}">
                                                    <td>
                                                        <input class="segment-checkbox" type="checkbox" name="segment-checkbox" value="${verification.getRFControlVerificationId()}"/>
                                                    </td>
                                                </c:if>
                                                <td><c:out value="${verification.creditedControl.name}"/>; <c:out value="${verification.creditedControl.group.name}"/></td>
                                                <td class="icon-cell"><span title="${verification.verificationStatusId eq 1 ? 'Verified' : (verification.verificationStatusId eq 50 ? 'Provisionally Verified' : 'Not Verified')}" class="small-icon baseline-small-icon ${verification.verificationStatusId eq 1 ? 'verified-icon' : (verification.verificationStatusId eq 50 ? 'provisional-icon' : 'not-verified-icon')}"></span></td>
                                                <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${verification.verificationDate}"/></td>
                                                <td><c:out value="${s:formatUsername(verification.verifiedBy)}"/></td>
                                                <td><c:out value="${verification.comments}"/></td>
                                                <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${verification.expirationDate}"/></td>
                                                <td><a class="dialog-ready" data-dialog-title="Segment Verification History" href="${pageContext.request.contextPath}/verifications/control/segment-history?rfControlVerificationId=${verification.getRFControlVerificationId()}" title="Click for verification history">History</a></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                                <c:if test="${adminOrLeader && param.notEditable eq null}">
                                    <div id="multi-instructions">Hold down the control (Ctrl) or shift key when clicking to select multiple.  Hold down the Command (⌘) key on Mac.</div> 
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:when>
                <c:otherwise>                   
                    Choose an RF Segment to continue
                </c:otherwise>
            </c:choose>
            <div id="verify-dialog" class="dialog" title="Edit Credited Control Verification">
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
                                    <option value="50">Provisionally Verified</option>
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
                                <input id="expirationDate" name="expirationDate" type="text" class="date-time-field" placeholder="${s:getFriendlyDateTimePlaceholder()}"/>
                            </div>
                        </li>
                        <li>
                            <div class="li-key">Comments:</div>
                            <div class="li-value">
                                <textarea id="comments" name="comments"></textarea>
                            </div>
                        </li>                    
                    </ul>
                    <input type="hidden" id="creditedControlId" name="creditedControlId"/>
                    <div class="dialog-button-panel">
                        <span id="rows-differ-message">Note: One or more selected rows existing values differ</span>
                        <button id="verifySaveButton" class="dialog-submit ajax-button" type="button">Save</button>
                        <button class="dialog-close-button" type="button">Cancel</button>
                    </div>
                </form>
            </div>
        </section>
        <div id="success-dialog" class="dialog" title="Verification Saved Successfully">
            <span class="logentry-success">Verification contained downgrade so a new log entry was created: <a id="new-entry-url" href="#"></a></span>
            <div class="dialog-button-panel">
                <button class="dialog-close-button" type="button">OK</button>
            </div>
        </div>        
    </jsp:body>         
</t:page>
