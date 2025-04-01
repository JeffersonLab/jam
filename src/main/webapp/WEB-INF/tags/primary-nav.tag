<%@tag description="Primary Navigation Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
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