<%@tag description="The Site Page Template Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@attribute name="title"%>
<%@attribute name="category" %>
<%@attribute name="stylesheets" fragment="true" %>
<%@attribute name="scripts" fragment="true" %>
<%@attribute name="secondaryNavigation" fragment="true" %>
<c:choose>
    <c:when test="${param.partial eq 'Y'}">
        <div id="partial" data-title="${title}">
            <div id="partial-css">
                <jsp:invoke fragment="stylesheets"/>
            </div>
            <div id="partial-html">
                <div class="partial">
                    <jsp:doBody/>
                </div>
            </div>
            <div id="partial-js">
                <jsp:invoke fragment="scripts"/>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <s:tabbed-page title="${title}" category="${category}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/jam.css"/>
        <jsp:invoke fragment="stylesheets"/>
    </jsp:attribute>
            <jsp:attribute name="scripts">
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/jam.js"></script>
        <jsp:invoke fragment="scripts"/>
    </jsp:attribute>
            <jsp:attribute name="primaryNavigation">
                    <ul>
                        <li${fn:startsWith(currentPath, '/authorizations') ? ' class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/authorizations/cebaf">Authorizations</a></li>
                        <li${fn:startsWith(currentPath, '/verifications') ? ' class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/verifications">Verifications</a></li>
                        <li${fn:startsWith(currentPath, '/inventory') ? ' class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/inventory/controls">Inventory</a></li>
                        <li${'/help' eq currentPath ? ' class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/help">Help</a></li>
                    </ul>
    </jsp:attribute>
            <jsp:attribute name="secondaryNavigation">
        <jsp:invoke fragment="secondaryNavigation"/>
    </jsp:attribute>
            <jsp:body>
                <jsp:doBody/>
            </jsp:body>
        </s:tabbed-page>
    </c:otherwise>
</c:choose>