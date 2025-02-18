<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="jam" uri="http://jlab.org/jam/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<t:page title="Destination Verification History">
    <jsp:attribute name="stylesheets">
    </jsp:attribute>
    <jsp:attribute name="scripts">
    </jsp:attribute>        
    <jsp:body>
        <div class="banner-breadbox">
            <ul>
                <li>
                    <a href="${pageContext.request.contextPath}/verifications">Verifications</a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/verifications/control?creditedControlId=${verification.creditedControl.creditedControlId}">${verification.creditedControl.name}</a>
                </li>
                <li>
                    <c:out value="${verification.beamDestination.name}"/> <span>History</span>
                </li>
            </ul>
        </div>        
        <section>
            <div class="dialog-content">
                <h3>Credited Control</h3>
                <c:out value="${verification.creditedControl.name}"/>
                <h3>Beam Destination</h3>
                <c:out value="${verification.beamDestination.name}"/>
                <h3>Verification Team</h3>
                <c:out value="${verification.creditedControl.verificationTeam.name}"/>
                <h3>History</h3>
                <c:choose>
                    <c:when test="${fn:length(historyList) < 1}">
                        <div class="message-box">None</div>
                    </c:when>
                    <c:otherwise>
                        <div class="message-box"><c:out value="${selectionMessage}"/></div>
                        <table id="verification-table" class="data-table stripped-table">
                            <thead>
                                <tr>
                                    <th>Modified Date</th>
                                    <th>Modified By</th>
                                    <th>Verified Date</th>
                                    <th>Verified By</th>
                                    <th>Verified</th>                                            
                                    <th>Comments</th>
                                    <th>Expiration Date</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${historyList}" var="history">
                                    <tr>
                                        <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${history.modifiedDate}"/></td>
                                        <td><c:out value="${s:formatUsername(history.modifiedBy)}"/></td>
                                        <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${history.verificationDate}"/></td>
                                        <td><c:out value="${s:formatUsername(history.verifiedBy)}"/></td>
                                        <td class="icon-cell"><span title="${history.verificationStatusId eq 1 ? 'Verified' : (history.verificationStatusId eq 50 ? 'Provisionally Verified' : 'Not Verified')}" class="small-icon baseline-small-icon ${history.verificationStatusId eq 1 ? 'verified-icon' : (history.verificationStatusId eq 50 ? 'provisional-icon' : 'not-verified-icon')}"></span></td>
                                        <td>
                                            <c:out value="${history.comments}"/>
                                            <c:if test="${not empty history.externalUrl}">
                                                <div>
                                                    <a href="${history.externalUrl}">Documentation</a>
                                                </div>
                                            </c:if>
                                        </td>
                                        <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${history.expirationDate}"/></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                        <form id="filter-form" action="${pageContext.request.contextPath}/verifications/control/destination-history" method="get">
                            <input type="hidden" name="beamControlVerificationId" value="${fn:escapeXml(param.beamControlVerificationId)}"/>
                            <input type="hidden" id="offset-input" name="offset" value="0"/>
                        </form>
                        <button id="previous-button" type="button" data-offset="${paginator.previousOffset}" value="Previous"${paginator.previous ? '' : ' disabled="disabled"'}>Previous</button>                        
                        <button id="next-button" type="button" data-offset="${paginator.nextOffset}" value="Next"${paginator.next ? '' : ' disabled="disabled"'}>Next</button>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>          
    </jsp:body>         
</t:page>
