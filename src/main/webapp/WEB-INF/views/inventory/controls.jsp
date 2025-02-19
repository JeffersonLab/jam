<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<c:set var="title" value="Credited Controls"/>
<t:inventory-page title="${title}">
    <jsp:attribute name="stylesheets">
    </jsp:attribute>
    <jsp:attribute name="scripts">
               <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/controls.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <section>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
            <div class="message-box"><c:out value="${selectionMessage}"/></div>
            <div id="chart-wrap" class="chart-wrap-backdrop">
                <c:set var="readonly" value="${!pageContext.request.isUserInRole('jam-admin')}"/>
                <c:if test="${not readonly}">
                    <s:editable-row-table-controls>
                    </s:editable-row-table-controls>
                </c:if>
                <div class="dialog-content">
                    <table class="data-table stripped-table ${readonly ? '' : 'uniselect-table editable-row-table'}">
                        <thead>
                        <tr>
                            <c:if test="${empty param.controlId}">
                                <th>Name</th>
                            </c:if>
                            <th>Description</th>
                            <th>Documentation</th>
                            <th>Verification Team</th>
                            <th>Verification Frequency</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${controlList}" var="control">
                            <tr data-id="${control.creditedControlId}" data-team-id="${control.verificationTeam.verificationTeamId}" data-doc="${fn:escapeXml(control.docLabelUrlCsv)}">
                                <c:if test="${empty param.controlId}">
                                <td><c:out value="${control.name}"/></td>
                                </c:if>
                                <td><c:out value="${control.description}"/></td>
                                <td>
                                    <ul class="jam-cell-list">
                                    <c:forEach items="${control.docLinkList}" var="link">
                                        <li>
                                            <c:choose>
                                                <c:when test="${not empty link.url}">
                                                    <a href="${link.url}"><c:out value="${link.label}"/></a>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:out value="${link.label}"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </li>
                                    </c:forEach>
                                    </ul>
                                </td>
                                <td>
                                    <c:url var="url" value="/inventory/verifiers">
                                        <c:param name="name" value="${control.verificationTeam.name}"/>
                                    </c:url>
                                    <a href="${url}" class="dialog-ready" data-dialog-title="${control.verificationTeam.name}"><c:out value="${control.verificationTeam.name}"/></a>
                                </td>
                                <td><c:out value="${empty control.verificationFrequency ? 'As Needed' : control.verificationFrequency}"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
        <s:editable-row-table-dialog>
            <form id="row-form">
                <ul class="key-value-list">
                    <li>
                        <div class="li-key">
                            <label for="row-name">Name</label>
                        </div>
                        <div class="li-value">
                            <input type="text" id="row-name" autocomplete="off"/>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="row-description">Description</label>
                        </div>
                        <div class="li-value">
                            <textarea id="row-description" autocomplete="off"></textarea>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="row-doc">Documentation</label>
                        </div>
                        <div class="li-value">
                            <input type="text" id="row-doc" autocomplete="off" placeholder="{label}|{url},..."/>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="row-team">Team</label>
                        </div>
                        <div class="li-value">
                            <select id="row-team" required="required">
                                <option value="">&nbsp;</option>
                                <c:forEach items="${teamList}" var="team">
                                    <option value="${team.verificationTeamId}">
                                        <c:out value="${team.name}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="row-frequency">Frequency</label>
                        </div>
                        <div class="li-value">
                            <input type="text" id="row-frequency"/>
                        </div>
                    </li>
                </ul>
            </form>
        </s:editable-row-table-dialog>
    </jsp:body>         
</t:inventory-page>