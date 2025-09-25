<%@tag description="Primary Navigation Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="s" uri="jlab.tags.smoothness"%>
<ul>
    <li${fn:startsWith(currentPath, '/authorizations') ? ' class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/authorizations/cebaf">Authorizations</a></li>
    <li${fn:startsWith(currentPath, '/verifications') ? ' class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/verifications">Verifications</a></li>
    <li${fn:startsWith(currentPath, '/inventory') ? ' class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/inventory/controls">Inventory</a></li>
    <c:if test="${pageContext.request.isUserInRole('jam-admin')}">
        <li${fn:startsWith(currentPath, '/setup') ? ' class="current-primary"' : ''}>
            <a href="${pageContext.request.contextPath}/setup/settings">Setup</a>
        </li>
    </c:if>
    <li${'/help' eq currentPath ? ' class="current-primary"' : ''}><a href="${pageContext.request.contextPath}/help">Help</a></li>
</ul>