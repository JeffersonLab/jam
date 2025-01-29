<%@tag description="The Site Page Template Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@attribute name="title"%>
<%@attribute name="category" %>
<%@attribute name="stylesheets" fragment="true" %>
<%@attribute name="scripts" fragment="true" %>
<%@attribute name="secondaryNavigation" fragment="true" %>
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
                        <li${fn:startsWith(currentPath, '/credited-controls') ? ' class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/credited-controls">Credited Controls</a></li>
                        <li${fn:startsWith(currentPath, '/inventory') ? ' class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/inventory/controls">Inventory</a></li>
                        <li${'/destinations' eq currentPath ? ' class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/destinations">Beam Destinations</a></li>
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