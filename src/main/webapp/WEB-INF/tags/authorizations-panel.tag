<%@tag description="Permissions Table Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="jam" uri="http://jlab.org/jam/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@attribute name="rfList" required="true" type="java.util.List"%>
<%@attribute name="beamList" required="true" type="java.util.List"%>
<%@attribute name="isRfEditable" required="true" type="java.lang.Boolean"%>
<%@attribute name="isBeamEditable" required="true" type="java.lang.Boolean"%>
<%@attribute name="isHistory" required="true" type="java.lang.Boolean"%>
<div id="authorization-panel">
    <div class="chart-wrap-backdrop">
        <c:if test="${not empty rfList}">
            <div id="rf-accordion" class="accordion">
                <h3>RF Operations</h3>
                <div class="content">
                    <form id="rf-authorization-form" method="post" action="${pageContext.request.contextPath}/ajax/edit-rf-auth">
                        <input type="hidden" name="facilityId" value="${facility.facilityId}">
                        <div class="control-panel">
                            <div class="control-item edit-button-panel">
                                <c:if test="${isRfEditable}">
                                    <span class="readonly-field"><button id="rf-edit-button" type="button">Edit RF Operations</button></span>
                                </c:if>
                                <div class="editable-field">
                                    <div>
                                        <button id="rf-save-button" class="ajax-button inline-button" type="button">Save</button>
                                        <span class="cancel-text">or <a id="rf-cancel-button" href="#">Cancel</a></span>
                                    </div>
                                </div>
                            </div>
                            <div class="control history-panel">
                                <c:if test="${not isHistory && param.print ne 'Y'}">
                                    <a data-dialog-title="Authorization History" href="${pageContext.request.contextPath}/authorizations${facility.path}/rf-history" title="Click for authorization history">History</a>
                                </c:if>
                            </div>
                        </div>
                        <t:rf-operations-panel rfList="${rfList}" isHistory="${isHistory}"/>
                    </form>
                </div>
            </div>
        </c:if>
        <c:if test="${not empty beamList}">
            <div id="beam-accordion" class="accordion">
                <h3>Beam Operations</h3>
                <div class="content">
                    <form id="beam-authorization-form" method="post" action="${pageContext.request.contextPath}/ajax/edit-beam-auth">
                        <input type="hidden" name="facilityId" value="${facility.facilityId}">
                        <div class="control-panel">
                            <div class="control-item edit-button-panel">
                                <c:if test="${isBeamEditable}">
                                    <span class="readonly-field"><button id="beam-edit-button" type="button">Edit Beam Operations</button></span>
                                </c:if>
                                <div class="editable-field">
                                    <div>
                                        <button id="beam-save-button" class="ajax-button inline-button" type="button">Save</button>
                                        <span class="cancel-text">or <a id="beam-cancel-button" href="#">Cancel</a></span>
                                    </div>
                                </div>
                            </div>
                            <div class="control history-panel">
                                <c:if test="${not isHistory && param.print ne 'Y'}">
                                    <a data-dialog-title="Authorization History" href="${pageContext.request.contextPath}/authorizations${facility.path}/beam-history" title="Click for authorization history">History</a>
                                </c:if>
                            </div>
                        </div>
                        <div class="editable-field power-limited-note"><b>Note</b>: Blank/Empty Current Limit results in "Dump Power Limited"</div>
                        <t:beam-operations-panel beamList="${beamList}" isHistory="${isHistory}"/>
                    </form>
                </div>
            </div>
        </c:if>
    </div>
</div>