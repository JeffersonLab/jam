<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="jam" uri="http://jlab.org/jam/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<c:set var="title" value="Control Verification"/>
<t:page title="${title}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/control-verification.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">          
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/control-verification.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <c:if test="${creditedControl ne null}">
            <div class="banner-breadbox">
                <ul>
                    <li>
                        <a href="${pageContext.request.contextPath}/verifications">Verifications</a>
                    </li>
                    <li>
                        <form method="get" action="control">
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
            <h2><c:out value="${title}${empty creditedControl ? '' : ': '.concat(creditedControl.name)}"/> (<a href="${pageContext.request.contextPath}/inventory/controls?controlId=${creditedControl.creditedControlId}" class="dialog-ready" data-dialog-title="${creditedControl.name}">🗗</a>)</h2>
            <c:choose>
                <c:when test="${creditedControl ne null}">
                    <div class="dialog-content">
                        <h3>Facility Verifications</h3>
                        <ul id="facility-control-verification-list">
                            <c:forEach items="${creditedControl.facilityControlVerificationList}" var="fv">
                                <li>
                                    <c:choose>
                                        <c:when test="${fv.verificationStatusId eq 1}">
                                            <span title="Verified" class="small-icon baseline-small-icon verified-icon"></span>
                                        </c:when>
                                        <c:when test="${fv.verificationStatusId eq 50}">
                                            <span title="Verified" class="small-icon baseline-small-icon provisional-icon"></span>
                                        </c:when>
                                        <c:otherwise>
                                            <span title="Not Verified" class="small-icon baseline-small-icon not-verified-icon"></span>
                                        </c:otherwise>
                                    </c:choose>
                                    <c:out value="${fv.getFacilityControlVerificationPK().facility.name}"/>
                                    <c:if test="${fv.verificationStatusId ne 100}">
                                        <fmt:formatDate value="${fv.expirationDate}" pattern="${s:getFriendlyDateTimePattern()}"/>
                                        <span class="expiring-soon" style="<c:out value="${jam:isExpiringSoon(fv.expirationDate) ? 'display: inline-block;' : 'display: none;'}"/>">(Expiring Soon)</span>
                                    </c:if>
                                </li>
                            </c:forEach>
                        </ul>
                        <h3>Operations Verifications</h3>
                        <div class="accordion">
                            <h3>RF Operations</h3>
                            <div class="content">
                                <c:choose>
                                    <c:when test="${fn:length(creditedControl.getRFControlVerificationList()) < 1}">
                                        None
                                    </c:when>
                                    <c:otherwise>
                                        <t:verification-panel operationsType="rf" operationsList="${creditedControl.getRFControlVerificationList()}"/>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div class="accordion">
                            <h3>Beam Operations</h3>
                            <div class="content">
                        <c:choose>
                            <c:when test="${fn:length(creditedControl.beamControlVerificationList) < 1}">
                                None
                            </c:when>
                            <c:otherwise>
                                <t:verification-panel operationsType="beam" operationsList="${creditedControl.beamControlVerificationList}"/>
                            </c:otherwise>
                        </c:choose>
                            </div>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    Choose a Credited Control to continue
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
                <input type="hidden" id="verificationType" name="verificationType"/>
            </form>
        </div>
        <div id="success-dialog" class="dialog" title="Verification Saved Successfully">
            <span class="logentry-success">Verification contained downgrade so a new log entry was created: <a id="new-entry-url" href="#"></a></span>
            <div class="dialog-button-panel">
                <button class="dialog-close-button" type="button">OK</button>
            </div>
        </div>
        <div id="component-edit-dialog" class="dialog" title="Components">
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
                <button class="dialog-close-button" type="button">OK</button>
            </div>
        </div>
    </jsp:body>         
</t:page>
