<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="jam" uri="http://jlab.org/jam/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<t:facility-authorizations-page title="Beam Authorization History">
    <jsp:attribute name="stylesheets">
        <style>
            .auth-notes-span {
                white-space: pre-line;
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript">
            $(document).on("click", ".next-button, .previous-button", function () {
                $(".offset-input").val($(this).attr("data-offset"));
                $(".filter-form").submit();
            });
        </script>
    </jsp:attribute>        
    <jsp:body>
        <div class="banner-breadbox hide-in-dialog">
            <ul>
                <li>
                    <a href="${pageContext.request.contextPath}/authorizations${facility.path}"><c:out value="${facility.name}"/></a>
                </li>
                <li>
                    <span>Beam History</span>
                </li>
            </ul>
        </div>        
        <section>
            <div>
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
                                        <td><span class="auth-notes-span"><c:out value="${history.comments}"/></span></td>
                                        <td><a href="${pageContext.request.contextPath}/authorizations${facility.path}/beam-history/destinations?beamAuthorizationId=${history.beamAuthorizationId}">Destination Details</a></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                        <form class="filter-form" action="" method="get">
                            <input type="hidden" class="offset-input" name="offset" value="0"/>
                        </form>
                        <button class="previous-button" type="button" data-offset="${paginator.previousOffset}" value="Previous"${paginator.previous ? '' : ' disabled="disabled"'}>Previous</button>
                        <button class="next-button" type="button" data-offset="${paginator.nextOffset}" value="Next"${paginator.next ? '' : ' disabled="disabled"'}>Next</button>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>          
    </jsp:body>         
</t:facility-authorizations-page>
