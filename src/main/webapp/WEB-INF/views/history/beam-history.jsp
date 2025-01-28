<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="beamauth" uri="http://jlab.org/beamauth/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<t:facility-authorizations-page title="Authorization History">
    <jsp:attribute name="stylesheets">
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript">
            $(document).on("click", "#next-button, #previous-button", function () {
                $("#offset-input").val($(this).attr("data-offset"));
                $("#filter-form").submit();
            });
        </script>
    </jsp:attribute>        
    <jsp:body>
        <div class="banner-breadbox">
            <ul>
                <li>
                    <a href="${pageContext.request.contextPath}/authorizations${facility.path}"><c:out value="${facility.name}"/> Authorization</a>
                </li>
                <li>
                    <span>Beam History</span>
                </li>
            </ul>
        </div>        
        <section>
            <div class="dialog-content">
                <c:choose>
                    <c:when test="${fn:length(historyList) < 1}">
                        <div class="message-box">None</div>
                    </c:when>
                    <c:otherwise>
                        <div class="message-box"><c:out value="${selectionMessage}"/></div>
                        <table id="beamAuthorization-table" class="data-table stripped-table">
                            <thead>
                                <tr>
                                    <th>Modified Date</th>
                                    <th>Modified By</th>
                                    <th>Authorization Date</th>
                                    <th>Authorized By</th>
                                    <th>Notes</th>
                                    <th>Destination Authorizations</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${historyList}" var="history">
                                    <tr>
                                        <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${history.modifiedDate}"/></td>
                                        <td><c:out value="${s:formatUsername(history.modifiedBy)}"/></td>
                                        <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${history.authorizationDate}"/></td>
                                        <td><c:out value="${s:formatUsername(history.authorizedBy)}"/></td>
                                        <td><c:out value="${history.comments}"/></td>
                                        <td><a href="${pageContext.request.contextPath}/authorizations${facility.path}/beam-history/destinations?beamAuthorizationId=${history.beamAuthorizationId}">Destination Details</a></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                        <form id="filter-form" action="beamAuthorization-history" method="get">
                            <input type="hidden" id="offset-input" name="offset" value="0"/>
                        </form>
                        <button id="previous-button" type="button" data-offset="${paginator.previousOffset}" value="Previous"${paginator.previous ? '' : ' disabled="disabled"'}>Previous</button>                        
                        <button id="next-button" type="button" data-offset="${paginator.nextOffset}" value="Next"${paginator.next ? '' : ' disabled="disabled"'}>Next</button> 
                    </c:otherwise>
                </c:choose>
            </div>
        </section>          
    </jsp:body>         
</t:facility-authorizations-page>
