<%@tag description="The Inventory Page Template" pageEncoding="UTF-8"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@attribute name="title"%>
<%@attribute name="stylesheets" fragment="true"%>
<%@attribute name="scripts" fragment="true"%>
<t:page title="${title}">
    <jsp:attribute name="stylesheets">       
        <jsp:invoke fragment="stylesheets"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <jsp:invoke fragment="scripts"/>
    </jsp:attribute>
    <jsp:attribute name="secondaryNavigation">
        <h2 id="left-column-header">Inventory</h2>
        <ul>
            <li${fn:startsWith(currentPath, '/inventory/controls') ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/inventory/controls">Controls</a></li>
            <li${fn:startsWith(currentPath, '/inventory/facilities') ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/inventory/facilities">Facilities</a></li>
            <li${fn:startsWith(currentPath, '/inventory/segments') ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/inventory/segments">Segments</a></li>
            <li${fn:startsWith(currentPath, '/inventory/destinations') ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/inventory/destinations">Destinations</a></li>
            <li${fn:startsWith(currentPath, '/inventory/participation') ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/inventory/participation">Participation</a></li>
            <li${fn:startsWith(currentPath, '/inventory/authorizers') ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/inventory/authorizers">Authorizers</a></li>
            <li${fn:startsWith(currentPath, '/inventory/watchers') ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/inventory/watchers">Watchers</a></li>
            <li${fn:startsWith(currentPath, '/inventory/verifiers') ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/inventory/verifiers">Verifiers</a></li>
        </ul>
    </jsp:attribute>
    <jsp:body>
        <jsp:doBody/>
    </jsp:body>         
</t:page>
