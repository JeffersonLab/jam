<%@tag description="The Authorization Page Template Tag" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@taglib prefix="s" uri="jlab.tags.smoothness"%>
<%@attribute name="title" %>
<%@attribute name="stylesheets" fragment="true" %>
<%@attribute name="scripts" fragment="true" %>
<s:page title="Auth - ${title}">
    <jsp:attribute name="stylesheets">       
        <jsp:invoke fragment="stylesheets"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <jsp:invoke fragment="scripts"/>
    </jsp:attribute>
    <jsp:body>
        <c:set var="currentPathInfo" scope="request" value="${requestScope['jakarta.servlet.forward.path_info']}"/>
        <div id="two-columns">
            <div id="left-column">
                <section>
                    <h2 id="left-column-header">Auth</h2>
                    <nav id="secondary-nav">
                        <ul>
                        <c:forEach items="${facilityList}" var="facility">
                            <li${fn:startsWith(currentPathInfo, facility.path) ? ' class="current-secondary"' : ''}><a
                                    href="${pageContext.request.contextPath}/authorizations${facility.path}">${facility.name}</a>
                            </li>
                        </c:forEach>
                        </ul>
                    </nav>
                </section>
            </div>
            <div id="right-column">
                <jsp:doBody/>
            </div>
        </div>
    </jsp:body>
</s:page>
