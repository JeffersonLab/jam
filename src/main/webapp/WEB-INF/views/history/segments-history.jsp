<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="beamauth" uri="http://jlab.org/beamauth/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<t:facility-authorizations-page title="Segment Authorization History">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/authorizations.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
    </jsp:attribute>        
    <jsp:body>
        <div class="banner-breadbox">
            <ul>
                <li>
                    <a href="${pageContext.request.contextPath}/authorizations${facility.path}"><c:out value="${facility.name}"/></a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/authorizations${facility.path}/rf-history">RF History</a>
                </li>
                <li>
                    <span><fmt:formatDate value="${rfAuthorization.modifiedDate}" pattern="${s:getFriendlyDateTimePattern()}"/></span>
                </li>
            </ul>
        </div>        
        <section>
            <c:choose>
                <c:when test="${rfAuthorization ne null}">
                    <t:rf-operations-panel rfList="${rfList}" isHistory="${true}"/>
                </c:when>
                <c:otherwise>
                    <div class="message-box">No Authorization found with ID: ${fn:escapeXml(param.rfAuthorizationId)}</div>
                </c:otherwise>
            </c:choose>
        </section>          
    </jsp:body>         
</t:facility-authorizations-page>
