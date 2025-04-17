<%@tag description="App Style Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/jam.js"></script>
<c:if test="${settings.is('NOTIFICATION_ENABLED')}">
<script>
    $(function() {
        $("#notification-bar").remove();
        let div = '<div id="notification-bar"><c:out value="${settings.get('NOTIFICATION_MESSAGE')}"/> ';

        <c:if test="${not empty settings.get('NOTIFICATION_LINK_NAME')}">
           <c:set var="TEXT" value="${settings.get('NOTIFICATION_LINK_NAME')}"/>
           <c:set var="HREF" value="${settings.get('NOTIFICATION_LINK_URL')}"/>
           div = div + '(<a href="${fn:escapeXml(HREF)}">${fn:escapeXml(TEXT)}</a>)';
        </c:if>

        $("body").prepend(div);
    });
</script>
</c:if>