<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="beamauth" uri="http://jlab.org/beamauth/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<t:page title="Beam Destination Information: ${beamDestination.name}">
    <jsp:attribute name="stylesheets">
        <style type="text/css">
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">       
    </jsp:attribute>        
    <jsp:body>
        <section>
            <h2>${beamDestination.name} Information</h2>
            <div class="dialog-content">
                <h3>
                    <c:choose>
                        <c:when test="${beamDestination.verification.verificationStatusId eq 1}">
                            <span title="Verified" class="small-icon baseline-small-icon verified-icon"></span>
                        </c:when>
                        <c:when test="${beamDestination.verification.verificationStatusId eq 50}">
                            <span title="Verified" class="small-icon baseline-small-icon provisional-icon"></span>
                        </c:when>
                        <c:otherwise>
                            <span title="Not Verified" class="small-icon baseline-small-icon not-verified-icon"></span>
                        </c:otherwise>
                    </c:choose>  
                    <a href="${pageContext.request.contextPath}/destinations?destinationId=${beamDestination.beamDestinationId}"><c:out value="${beamDestination.name}"/></a> Credited Control Status
                </h3>
                <c:choose>
                    <c:when test="${fn:length(verificationList) > 0}">
                        <table class="data-table stripped-table">
                            <thead>
                                <tr>
                                    <th>Credited Control</th>
                                    <th>Status</th>
                                    <th>Expiration Date</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${verificationList}" var="verification">
                                    <tr>
                                        <td><a href="${pageContext.request.contextPath}/credited-controls?creditedControlId=${verification.creditedControl.creditedControlId}"><c:out value="${verification.creditedControl.name}"/></a></td>
                                        <td class="icon-cell">
                                            <c:choose>
                                                <c:when test="${verification.verificationStatusId eq 1}">
                                                    <span title="Verified" class="small-icon verified-icon"></span>
                                                </c:when>
                                                <c:when test="${verification.verificationStatusId eq 50}">
                                                    <span title="Provisionally Verified" class="small-icon provisional-icon"></span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span title="Not Verified" class="small-icon not-verified-icon"></span>
                                                </c:otherwise>
                                            </c:choose>                                
                                        </td>           
                                        <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${verification.expirationDate}"/><span class="expiring-soon" style="<c:out value="${verification.expirationDate ne null and verification.expirationDate.time > beamauth:now().time and verification.expirationDate.time < beamauth:twoDaysFromNow().time ? 'display: block;' : 'display: none;'}"/>">(Expiring Soon)</span></td>
                                    </tr>
                                </c:forEach> 
                            </tbody>
                        </table>                        
                    </c:when>
                    <c:otherwise>
                        <div class="message-box">No Credited Controls Required</div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </jsp:body>         
</t:page>
