<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="beamauth" uri="http://jlab.org/beamauth/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<t:page title="Credited Controls"> 
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/credited-controls.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">          
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/credited-controls.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <c:if test="${creditedControl ne null}">
            <div class="banner-breadbox">
                <ul>
                    <li>
                        <a href="credited-controls">Credited Controls</a>
                    </li>
                    <li>
                        <form method="get" action="credited-controls">
                            <select name="creditedControlId" class="change-submit">
                                <c:forEach items="${ccList}" var="cc">
                                    <option value="${cc.creditedControlId}"${param.creditedControlId eq cc.creditedControlId ? ' selected="selected"' : ''}><c:out value="${cc.name}"/></option>
                                </c:forEach>
                            </select>
                        </form>
                    </li>
                </ul>
            </div>
        </c:if>
        <section>
            <c:choose>
                <c:when test="${creditedControl ne null}">
                    <div class="dialog-content">
                        <h3>Group</h3>
                        <c:out value="${creditedControl.group.name}"/>
                        <h3>Description</h3>
                        <c:choose>
                            <c:when test="${fn:length(creditedControl.description) > 0}">
                                <c:out value="${creditedControl.description}"/>
                            </c:when>
                            <c:otherwise>
                                <div class="message-box">None</div>
                            </c:otherwise>
                        </c:choose>
                        <h3>Verification Frequency</h3>
                        <c:choose>
                            <c:when test="${fn:length(creditedControl.verificationFrequency) > 0}">
                                <c:out value="${creditedControl.verificationFrequency}"/>
                            </c:when>
                            <c:otherwise>
                                <div class="message-box">As Needed</div>
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${pageContext.request.isUserInRole('jam-admin')}">
                            <h3>Operability Notes <span class="readonly-field"><button id="operability-notes-edit-button" type="button">Edit</button></span></h3>    
                            <div class="notes-field">
                                <form id="operability-form" method="post" action="ajax/edit-operability-note">
                                    <span class="readonly-field">
                                        <c:out value="${fn:length(creditedControl.comments) == 0 ? 'None' : creditedControl.comments}"/>
                                    </span>
                                    <span class="editable-field">
                                        <textarea id="operability-comments" name="comments"><c:out value="${creditedControl.comments}"/></textarea>
                                        <input type="hidden" name="creditedControlId" value="${creditedControl.creditedControlId}"/>
                                    </span>
                                </form>
                            </div>
                            <div class="edit-button-block editable-field">
                                <span>
                                    <button id="save-button" class="ajax-button inline-button" type="button">Save</button>
                                    <span class="cancel-text">
                                        or 
                                        <a id="cancel-button" href="#">Cancel</a>
                                    </span>
                                </span>
                            </div>
                        </c:if>
                        <h3>Verifications</h3>
                        <c:choose>
                            <c:when test="${fn:length(creditedControl.controlVerificationList) < 1}">
                                <div class="message-box">None</div>
                            </c:when>
                            <c:otherwise>
                                <c:if test="${adminOrLeader && param.notEditable eq null}">
                                    <button id="edit-selected-button" type="button" class="verify-button selected-row-action" disabled="disabled">Edit Verification</button>
                                </c:if>
                                <c:if test="${pageContext.request.isUserInRole('jam-admin') && param.notEditable eq null}">
                                    <button id="component-edit-button" type="button" class="single-select-row-action" disabled="disabled">Edit Components</button>
                                </c:if>
                                <table id="verification-table" class="data-table stripped-table${(adminOrLeader && param.notEditable eq null) ? ' multicheck-table editable-row-table' : ''}">
                                    <thead>
                                        <tr>
                                            <c:if test="${adminOrLeader && param.notEditable eq null}">
                                                <th class="select-header">
                                                    Select
                                                    <select id="check-select" name="check-select">
                                                        <option value="">&nbsp;</option>
                                                        <option value="all">All</option>
                                                        <option value="none">None</option>
                                                    </select>
                                                </th>
                                            </c:if>
                                            <th>Beam Destination</th>
                                            <th>Verified</th>
                                            <th>Components</th>
                                            <th>Comments</th>
                                            <th>Expiration Date</th>
                                            <th class="audit-header">Audit</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${creditedControl.controlVerificationList}" var="verification">
                                            <tr data-control-verification-id="${verification.controlVerificationId}" data-verified-username="${verification.verifiedBy}" data-status-id="${verification.verificationId}">
                                                <c:if test="${adminOrLeader && param.notEditable eq null}">
                                                    <td>
                                                        <input class="destination-checkbox" type="checkbox" name="destination-checkbox" value="${verification.controlVerificationId}"/>
                                                    </td>
                                                </c:if>
                                                <td><c:out value="${verification.beamDestination.name}"/></td>
                                                <td class="verified-cell">
                                                  <div title="${verification.verificationId eq 1 ? 'Verified' : (verification.verificationId eq 50 ? 'Provisionally Verified' : 'Not Verified')}" class="small-icon baseline-small-icon ${verification.verificationId eq 1 ? 'verified-icon' : (verification.verificationId eq 50 ? 'provisional-icon' : 'not-verified-icon')}"></div>
                                                  <div class="verified-date"><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${verification.verificationDate}"/></div>
                                                  <div class="verified-by"><c:out value="${s:formatUsername(verification.verifiedBy)}"/></div>
                                                </td>
                                                <td>
                                                    <c:forEach items="${verification.componentList}" var="component">
                                                        <div><span class="small-icon baseline-small-icon verified-icon"></span> <a href="${env['SRM_COMPONENT_URL']}${fn:escapeXml(component.name)}"><c:out value="${component.name}"/></a></div>
                                                    </c:forEach>
                                                </td>
                                                <td><c:out value="${verification.comments}"/></td>
                                                <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${verification.expirationDate}"/></td>
                                                <td><a data-dialog-title="Verification History" href="credited-controls/verification-history?controlVerificationId=${verification.controlVerificationId}" title="Click for verification history">History</a></td>
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
                    <div class="expire-links"><a id="expired-link" href="#">Expired</a> | <a id="expiring-link" href="#">Expiring</a></div>                    
                    <h2>Credited Controls</h2>
                    <table class="data-table stripped-table">
                        <thead>
                            <tr>
                                <th>Select</th>
                                <th>Name</th>
                                <th>Description</th>
                                <th>Group</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${ccList}" var="cc">
                                <tr data-credited-control-id="${cc.creditedControlId}">
                                    <td>
                                        <form method="get" action="credited-controls">
                                            <input type="hidden" name="creditedControlId" value="${cc.creditedControlId}"/>
                                            <button class="single-char-button" type="submit">&rarr;</button>
                                        </form>
                                    </td>
                                    <td><c:out value="${cc.name}"/></td>
                                    <td><c:out value="${cc.description}"/></td>
                                    <td><a data-dialog-title="${cc.group.name} Information" class="dialog-ready" href="group-information?groupId=${cc.group.workgroupId}"><c:out value="${cc.group.name}"/></a></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table> 
                </c:otherwise>
            </c:choose>
        </section>
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
        <div id="success-dialog" class="dialog" title="Verification Saved Successfully">
            <span class="logentry-success">Verification contained downgrade so a new log entry was created: <a id="new-entry-url" href="#"></a></span>
            <div class="dialog-button-panel">
                <button class="dialog-close-button" type="button">OK</button>
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
                                    <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${verification.expirationDate}"/></td>
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
                                    <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${verification.expirationDate}"/></td>
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
        <div id="component-edit-dialog" class="dialog" title="Components">
            <div class="row">
                <div class="column">
                    <fieldset>
                        <legend>Choose</legend>
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key">
                                    <label class="required-field" for="component">Component</label>
                                </div>
                                <div class="li-value">
                                    <input type="text" id="component" name="component" placeholder="search for name"/>
                                </div>
                            </li>
                        </ul>
                    </fieldset>
                </div>
                <div class="column">
                    <ul id="selected-component-list">
                        <li>Component 1</li>
                    </ul>
                </div>
            </div>
            <div class="dialog-button-panel">
                <button class="dialog-close-button" type="button">OK</button>
            </div>
        </div>
    </jsp:body>         
</t:page>
