<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="jam" uri="http://jlab.org/jam/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<t:facility-authorizations-page title="RF Authorization History">
    <jsp:attribute name="stylesheets">
        <style>
            .auth-notes-span {
                white-space: pre-line;
            }
            td:nth-child(2) {
                width: 130px;
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">
    </jsp:attribute>        
    <jsp:body>
        <div class="banner-breadbox hide-in-dialog">
            <ul>
                <li>
                    <a href="${pageContext.request.contextPath}/authorizations${facility.path}"><c:out value="${facility.name}"/></a>
                </li>
                <li>
                    <span>RF History</span>
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
                        <div class="top-right-box"><sup>†</sup>Automated Auth Reduction</div>
                        <div class="message-box"><c:out value="${selectionMessage}"/></div>
                        <table id="rfAuthorization-table" class="data-table stripped-table">
                            <thead>
                                <tr>
                                    <th>Modified Date</th>
                                    <th>Modified By</th>
                                    <th>Authorization Date</th>
                                    <th>Authorized By</th>
                                    <th>Change Notes</th>
                                    <th>Segment Authorizations</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${historyList}" var="history">
                                    <c:set var="automated" value="${history.modifiedBy ne history.authorizedBy}"/>
                                    <tr>
                                        <td><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${history.modifiedDate}"/></td>
                                        <td>
                                            <span><c:out value="${s:formatUsername(history.modifiedBy)}"/>
                                            <c:if test="${automated}">
                                                <sup>†</sup>
                                            </c:if>
                                            </span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${automated}">

                                                </c:when>
                                                <c:otherwise>
                                                    <fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${history.authorizationDate}"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${automated}">

                                                </c:when>
                                                <c:otherwise>
                                                    <c:out value="${s:formatUsername(history.authorizedBy)}"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><span class="auth-notes-span"><c:out value="${history.comments}"/></span></td>
                                        <td><a href="${pageContext.request.contextPath}/authorizations${facility.path}/rf-history/segments?rfAuthorizationId=${history.rfAuthorizationId}">Segment Details</a></td>
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
