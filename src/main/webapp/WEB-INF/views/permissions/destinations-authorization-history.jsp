<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="beamauth" uri="http://jlab.org/beamauth/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<t:page title="Destination Authorization History"> 
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/permissions.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
    </jsp:attribute>        
    <jsp:body>
        <div class="banner-breadbox">
            <ul>
                <li>
                    <a href="${pageContext.request.contextPath}/permissions">Director's Authorization</a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/permissions/authorization-history">Historic Authorizations</a>
                </li>
                <li>
                    <span><fmt:formatDate value="${authorization.modifiedDate}" pattern="${s:getFriendlyDateTimePattern()}"/> History</span>
                </li>
            </ul>
        </div>        
        <section>
            <c:choose>
                <c:when test="${authorization ne null}">
                    <t:permissions-page cebafDestinationList="${cebafDestinationList}" lerfDestinationList="${lerfDestinationList}" isEditable="${false}" isHistory="${true}"/>
                </c:when>
                <c:otherwise>
                    <div class="message-box">No Authorization found with ID: ${fn:escapeXml(param.authorizationId)}</div>
                </c:otherwise>
            </c:choose>
        </section>          
    </jsp:body>         
</t:page>
