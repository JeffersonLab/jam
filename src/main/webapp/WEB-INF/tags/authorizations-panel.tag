<%@tag description="Permissions Table Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="beamauth" uri="http://jlab.org/beamauth/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@attribute name="rfList" required="true" type="java.util.List"%>
<%@attribute name="beamList" required="true" type="java.util.List"%>
<%@attribute name="isEditable" required="true" type="java.lang.Boolean"%>
<%@attribute name="isHistory" required="true" type="java.lang.Boolean"%>
<form id="beamAuthorization-form" method="post" action="permissions">
    <div class="chart-wrap-backdrop">
        <h2><c:out value="${facility.name}"/>
            <c:if test="${isEditable}">
                <span class="readonly-field"><button id="edit-button" type="button">Edit</button></span>
            </c:if>            
        </h2>
        <div class="accordion">
            <h3>RF Operations</h3>
            <div class="content">
                <c:choose>
                    <c:when test="${not empty rfList}">
                        <t:rf-operations-table rfList="${rfList}" isHistory="${isHistory}"/>
                    </c:when>
                    <c:otherwise>
                        None
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        <div class="accordion">
            <h3>Beam Operations</h3>
            <div class="content">
                <c:choose>
                    <c:when test="${not empty beamList}">
                        <div class="editable-field power-limited-note">Note: Blank/Empty Current Limit results in "Dump Power Limited"</div>
                        <t:beam-operations-table beamList="${beamList}" isHistory="${isHistory}"/>
                        <h3>Notes</h3>
                        <div class="notes-field">
            <span class="readonly-field">
                <c:out value="${fn:length(beamAuthorization.comments) == 0 ? 'None' : beamAuthorization.comments}"/>
            </span>
                            <span class="editable-field">
                <textarea id="comments" name="comments"><c:out value="${beamAuthorization.comments}"/></textarea>
            </span>
                        </div>
                        <h3>Digital Signature</h3>
                        <div class="footer">
                            <div class="footer-row">
                                <div class="signature-field">
                                    <c:choose>
                                        <c:when test="${beamAuthorization ne null}">
                                            <div class="readonly-field">Authorized by ${s:formatUsername(beamAuthorization.authorizedBy)} on <fmt:formatDate value="${beamAuthorization.authorizationDate}" pattern="${s:getFriendlyDateTimePattern()}"/></div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="readonly-field">None</div>
                                        </c:otherwise>
                                    </c:choose>
                                    <div class="editable-field notification-option-panel">
                                        <p>
                                            <label for="generate-elog-checkbox">Generate elog and email:</label>
                                            <input id="generate-elog-checkbox" type="checkbox" name="notification" value="Y" checked="checked"/>
                                        </p>
                                    </div>
                                    <div class="editable-field">Click the Save button to sign:
                                        <span>
                            <button id="save-button" class="ajax-button inline-button" type="button">Save</button>
                            <span class="cancel-text">
                                or
                                <a id="cancel-button" href="#">Cancel</a>
                            </span>
                        </span>
                                    </div>
                                </div>
                                <div class="history-panel">
                                    <c:if test="${not isHistory}">
                                        <a data-dialog-title="Authorization History" href="${pageContext.request.contextPath}/authorizations${facility.path}/beam-history" title="Click for authorization history">History</a>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        None
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</form>