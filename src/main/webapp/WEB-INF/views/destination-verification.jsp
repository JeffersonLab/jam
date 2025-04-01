<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="jam" uri="http://jlab.org/jam/functions"%>
<c:set var="title" value="Destination Verification"/>
<s:page title="${title}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/verification-panel.css"/>
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
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/verification-panel.js"></script>
        <script type="text/javascript">
            jlab = jlab || {};
            jlab.verificationType = 'Control-Group';

            $(function () {
                jlab.verificationPanelInit();
            });
        </script>
    </jsp:attribute>        
    <jsp:body>
        <c:if test="${destination ne null}">
            <div class="banner-breadbox hide-in-dialog">
                <ul>
                    <li>
                        <a href="${pageContext.request.contextPath}/verifications">Verifications</a>
                    </li>
                    <li>
                        <form method="get" action="destination">
                            <select name="destinationId" class="change-submit">
                                <c:forEach items="${destinationList}" var="destination">
                                    <option value="${destination.beamDestinationId}"${param.destinationId eq destination.beamDestinationId ? ' selected="selected"' : ''}><c:out value="${destination.facility.name} / ${destination.name}"/></option>
                                </c:forEach>
                            </select>
                        </form>
                    </li>
                </ul>
            </div>
        </c:if>
        <section>
            <div class="top-right-box">
                <c:url value="/verifications/destination" var="url">
                    <c:param name="destinationId" value="${param.destinationId}"/>
                </c:url>
                <a class="dialog-only-inline-block" href="${url}" target="_blank">Open in new tab</a>
            </div>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
            <c:choose>
                <c:when test="${destination ne null}">
                    <div>
                        <h3>
                            <c:choose>
                                <c:when test="${destination.verification.verificationStatusId eq 1}">
                                    <span title="Verified" class="small-icon baseline-small-icon verified-icon"></span>
                                </c:when>
                                <c:when test="${destination.verification.verificationStatusId eq 50}">
                                    <span title="Verified" class="small-icon baseline-small-icon provisional-icon"></span>
                                </c:when>
                                <c:otherwise>
                                    <span title="Not Verified" class="small-icon baseline-small-icon not-verified-icon"></span>
                                </c:otherwise>
                            </c:choose>
                            Credited Control Verifications
                            <c:if test="${destination.verification.verificationStatusId ne 100}">
                                <span title="Earliest Control Expiration">
                                    <fmt:formatDate value="${destination.verification.expirationDate}" pattern="${s:getFriendlyDateTimePattern()}"/>
                                </span>
                                <span class="expiring-soon" style="<c:out value="${jam:isExpiringSoon(destination.verification.expirationDate) ? 'display: inline-block;' : 'display: none;'}"/>">(Expiring Soon)</span>
                            </c:if>
                        </h3>
                        <c:choose>
                            <c:when test="${fn:length(destination.beamControlVerificationList) < 1}">
                                None
                            </c:when>
                            <c:otherwise>
                                <t:verification-panel operationsType="beam" operationsList="${destination.beamControlVerificationList}" groupByOperation="true"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:when>
                <c:otherwise>                   
                    Choose a Beam Destination to continue
                </c:otherwise>
            </c:choose>
        </section>
    </jsp:body>         
</s:page>
