<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">My onboarding checklist</h1>

<c:choose>
  <c:when test="${empty tasks}">
    <div class="empty-state">You have no onboarding tasks.</div>
  </c:when>
  <c:otherwise>
    <div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
      <thead><tr><th>Task</th><th>Due date</th><th>Status</th><th>Notes</th><th class="text-end">Actions</th></tr></thead>
      <tbody>
      <c:forEach var="t" items="${tasks}">
        <tr>
          <td><c:out value="${t.title}"/><div class="small text-muted"><c:out value="${t.description}"/></div></td>
          <td><c:out value="${t.dueDate}"/></td>
          <td><span class="badge-status badge-${fn:toLowerCase(fn:replace(t.status, '_', '-'))}"><c:out value="${t.status}"/></span></td>
          <td class="small text-muted"><c:out value="${t.reviewNotes}"/></td>
          <td class="text-end">
            <c:if test="${t.status == 'NOT_STARTED'}">
              <form method="post" action="${pageContext.request.contextPath}/employee/onboarding/${t.id}/start" class="d-inline">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <button class="btn btn-sm btn-primary" type="submit">Start</button>
              </form>
            </c:if>
            <c:if test="${t.status == 'IN_PROGRESS' or t.status == 'REJECTED'}">
              <button class="btn btn-sm btn-outline-secondary" type="button" data-bs-toggle="collapse" data-bs-target="#evidence-${t.id}">Upload evidence</button>
              <form method="post" action="${pageContext.request.contextPath}/employee/onboarding/${t.id}/submit" class="d-inline">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <button class="btn btn-sm btn-primary" type="submit">Mark submitted</button>
              </form>
              <div class="collapse mt-2" id="evidence-${t.id}">
                <form method="post" action="${pageContext.request.contextPath}/employee/onboarding/${t.id}/evidence" enctype="multipart/form-data" class="d-flex gap-2">
                  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                  <input type="file" name="file" class="form-control form-control-sm" required>
                  <button class="btn btn-sm btn-outline-primary" type="submit">Upload</button>
                </form>
              </div>
            </c:if>
          </td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
    </div>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
