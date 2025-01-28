<%@tag description="Permissions Table Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="beamauth" uri="http://jlab.org/beamauth/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@attribute name="rfList" required="true" type="java.util.List"%>
<%@attribute name="beamList" required="true" type="java.util.List"%>
<%@attribute name="isEditable" required="true" type="java.lang.Boolean"%>
<%@attribute name="isHistory" required="true" type="java.lang.Boolean"%>
<div id="authorization-panel">
    <div class="chart-wrap-backdrop">
        <div class="accordion">
            <h3>RF Operations</h3>
            <div class="content">
                <form id="rf-authorization-form" method="post" action="${pageContext.request.contextPath}/ajax/edit-rf-auth">
                    <input type="hidden" name="facilityId" value="${facility.facilityId}">
                    <c:choose>
                        <c:when test="${not empty rfList}">
                            <c:if test="${isEditable}">
                                <span class="readonly-field"><button id="rf-edit-button" type="button">Edit RF Operations</button></span>
                            </c:if>
                            <t:rf-operations-panel rfList="${rfList}" isHistory="${isHistory}"/>
                        </c:when>
                        <c:otherwise>
                            None
                        </c:otherwise>
                    </c:choose>
                </form>
            </div>
        </div>
        <div class="accordion">
            <h3>Beam Operations</h3>
            <div class="content">
                <form id="beam-authorization-form" method="post" action="${pageContext.request.contextPath}/ajax/edit-beam-auth">
                    <input type="hidden" name="facilityId" value="${facility.facilityId}">
                <c:choose>
                    <c:when test="${not empty beamList}">
                        <c:if test="${isEditable}">
                            <span class="readonly-field"><button id="beam-edit-button" type="button">Edit Beam Operations</button></span>
                        </c:if>
                        <div class="editable-field power-limited-note">Note: Blank/Empty Current Limit results in "Dump Power Limited"</div>
                        <t:beam-operations-panel beamList="${beamList}" isHistory="${isHistory}"/>
                    </c:when>
                    <c:otherwise>
                        None
                    </c:otherwise>
                </c:choose>
                </form>
            </div>
        </div>
    </div>
</div>