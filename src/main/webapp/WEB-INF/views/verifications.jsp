<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="beamauth" uri="http://jlab.org/beamauth/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<t:page title="Verifications">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/credited-controls.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">          
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/credited-controls.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <section>
        <div class="expire-links"><a id="expired-link" href="#">Expired</a> | <a id="expiring-link" href="#">Expiring</a></div>
        <form method="get" action="credited-controls">
            <ul class="key-value-list">
                <li>
                    <div class="li-key">
                        <label for="control-select">By Credited Control</label>
                    </div>
                    <div class="li-value">
                        <select id="control-select" name="creditedControlId" class="change-submit">
                            <option value=""></option>
                            <c:forEach items="${ccList}" var="cc">
                                <option value="${cc.creditedControlId}"${param.creditedControlId eq cc.creditedControlId ? ' selected="selected"' : ''}><c:out value="${cc.name}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                </li>
            </ul>
        </form>
        <form method="get" action="destinations">
            <ul class="key-value-list">
                <li>
                    <div class="li-key">
                        <label for="destination-select">By Beam Destination</label>
                    </div>
                    <div class="li-value">
                        <select id="destination-select" name="destinationId" class="change-submit">
                            <option value=""></option>
                            <c:forEach items="${destinationList}" var="destination">
                                <option value="${destination.beamDestinationId}"${param.destinationId eq destination.beamDestinationId ? ' selected="selected"' : ''}><c:out value="${destination.name}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                </li>
            </ul>
        </form>
        <div id="expired-dialog" class="dialog" title="Expired Controls">
            <c:choose>
                <c:when test="${fn:length(expiredList) > 0}">
                    <table class="data-table stripped-table">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Beam Destination</th>
                                <th>Expiration Date</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${expiredList}" var="verification">
                                <tr>
                                    <td><c:out value="${verification.creditedControl.name}"/></td>                                    
                                    <td><c:out value="${verification.beamDestination.name}"/></td>
                                    <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${verification.expirationDate}"/></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>                            
                </c:when>
                <c:otherwise>
                    <div>No expired controls</div>
                </c:otherwise>
            </c:choose>
        </div>  
        <div id="expiring-dialog" class="dialog" title="Controls Expiring within Seven Days">
            <c:choose>
                <c:when test="${fn:length(expiringList) > 0}">
                    <table class="data-table stripped-table">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Beam Destination</th>
                                <th>Expiration Date</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${expiringList}" var="verification">
                                <tr>
                                    <td><c:out value="${verification.creditedControl.name}"/></td>                                    
                                    <td><c:out value="${verification.beamDestination.name}"/></td>
                                    <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${verification.expirationDate}"/></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>                            
                </c:when>
                <c:otherwise>
                    <div>No controls expiring within seven days</div>
                </c:otherwise>
            </c:choose>            
        </div>
    </jsp:body>         
</t:page>
