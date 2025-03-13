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
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/verification-panel.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">          
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/verification-panel.js"></script>
        <script>
            $(document).on("click", ".default-clear-panel", function () {
                $("#control-select").val('');
                $("#facility-select").val('');
                return false;
            });
            $(function () {
                jlab.verificationPanelInit();
            });
        </script>
    </jsp:attribute>
    <jsp:body>
        <section>
            <div class="ribbon-breadbox hide-in-dialog">
                <ul>
                    <li>
                        <s:filter-flyout-widget clearButton="true">
                            <form class="filter-form" method="get" action="control">
                                <div id="filter-form-panel">
                                    <fieldset>
                                        <legend>Filter</legend>
                                        <ul class="key-value-list">
                                            <li>
                                                <div class="li-key">
                                                    <label for="control-select">Control</label>
                                                </div>
                                                <div class="li-value">
                                                    <select id="control-select" name="creditedControlId">
                                                        <option value="">&nbsp;</option>
                                                        <c:forEach items="${ccList}" var="cc">
                                                            <option value="${cc.creditedControlId}"${param.creditedControlId eq cc.creditedControlId ? ' selected="selected"' : ''}><c:out value="${cc.verificationTeam.name} / ${cc.name}"/></option>
                                                        </c:forEach>
                                                    </select>
                                                </div>
                                            </li>
                                            <li>
                                                <div class="li-key">
                                                    <label for="facility-select">Facility</label>
                                                </div>
                                                <div class="li-value">
                                                    <select id="facility-select" name="facilityId">
                                                        <option value="">&nbsp;</option>
                                                        <c:forEach items="${facilityList}" var="facility">
                                                            <option value="${facility.facilityId}"${param.facilityId eq facility.facilityId ? ' selected="selected"' : ''}>
                                                                <c:out value="${facility.name}"/></option>
                                                        </c:forEach>
                                                    </select>
                                                </div>
                                            </li>
                                        </ul>
                                    </fieldset>
                                </div>
                                <input type="hidden" name="creditedControlId" value="${fn:escapeXml(param.creditedControlId)}"/>
                                <input class="filter-form-submit-button" type="submit" value="Apply"/>
                            </form>
                        </s:filter-flyout-widget>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/verifications?facilityId=${fn:escapeXml(param.facilityId)}">Verifications</a>
                    </li>
                </ul>
            </div>
            <c:if test="${not empty creditedControl}">
                <div class="top-right-box">
                    <a href="${pageContext.request.contextPath}/inventory/controls?controlId=${creditedControl.creditedControlId}&notEditable=" class="dialog-opener">Info</a>
                    <span class="dialog-only-inline-block">|</span>
                    <c:url value="/verifications/control" var="url">
                        <c:param name="creditedControlId" value="${param.creditedControlId}"/>
                    </c:url>
                    <a class="dialog-only-inline-block" href="${url}" target="_blank">Open in new tab</a>
                </div>
            </c:if>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
            <div class="message-box"><c:out value="${selectionMessage}"/></div>
            <c:choose>
                <c:when test="${creditedControl ne null}">
                    <div>
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
                                        <span title="Earliest Control Expiration">
                                            <fmt:formatDate value="${fv.expirationDate}" pattern="${s:getFriendlyDateTimePattern()}"/>
                                        </span>
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
                                        <t:verification-panel operationsType="rf" operationsList="${creditedControl.getRFControlVerificationList()}" groupByOperation="false"/>
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
                                <t:verification-panel operationsType="beam" operationsList="${creditedControl.beamControlVerificationList}" groupByOperation="false"/>
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
    </jsp:body>         
</t:page>
