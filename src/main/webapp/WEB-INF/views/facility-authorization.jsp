<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="beamauth" uri="http://jlab.org/beamauth/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<t:facility-authorizations-page title="${facility.name}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/authorizations.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">  
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/authorizations.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <section>
            <h2><c:out value="${facility.name}"/></h2>
            <t:authorizations-panel rfList="${rfList}" beamList="${beamList}" isEditable="${pageContext.request.isUserInRole('jam-admin')}" isHistory="${false}"/>
        </section>
        <div id="success-dialog" class="dialog" title="Authorization Saved Successfully">
            <span class="logentry-success">A new log entry was created: <a id="new-entry-url" href="#"></a></span>
            <div class="dialog-button-panel">
                <button class="dialog-close-button" type="button">OK</button>
            </div>
        </div>
    </jsp:body>         
</t:facility-authorizations-page>
