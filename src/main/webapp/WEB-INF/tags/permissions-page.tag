<%@tag description="The Permissions Page Template Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@attribute name="title"%>
<%@attribute name="stylesheets" fragment="true"%>
<%@attribute name="scripts" fragment="true"%>
<t:page title="${title}" category="Permissions">
    <jsp:attribute name="stylesheets">
        <jsp:invoke fragment="stylesheets"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <jsp:invoke fragment="scripts"/>
    </jsp:attribute>
    <jsp:attribute name="secondaryNavigation">
                        <ul>
                            <li${fn:startsWith(currentPath, '/permissions/cebaf') ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/permissions/cebaf">CEBAF</a></li>
                            <li${fn:startsWith(currentPath, '/permissions/lerf') ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/permissions/lerf">LERF</a></li>
                            <li${fn:startsWith(currentPath, '/permissions/uitf') ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/permissions/uitf">UITF</a></li>
                            <li${fn:startsWith(currentPath, '/permissions/cmtf') ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/permissions/cmtf">CMTF</a></li>
                            <li${fn:startsWith(currentPath, '/permissions/vta') ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/permissions/vta">VTA</a></li>
                            <li${fn:startsWith(currentPath, '/permissions/gts') ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/permissions/gts">GTS</a></li>
                        </ul>
    </jsp:attribute>
    <jsp:body>
        <jsp:doBody/>
    </jsp:body>
</t:page>