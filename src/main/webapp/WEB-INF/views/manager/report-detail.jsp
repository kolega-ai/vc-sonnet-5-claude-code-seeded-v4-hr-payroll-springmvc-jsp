<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<a class="small" href="${pageContext.request.contextPath}/manager/reports">&larr; Back to my team</a>
<h1 class="section-title h3 mt-2 mb-1"><c:out value="${employee.fullName}"/></h1>
<p class="text-muted"><c:out value="${employee.jobTitle}"/> &mdash; <c:out value="${employee.department.name}"/></p>

<form method="post" action="${pageContext.request.contextPath}/manager/reports/${employee.id}/summary" class="d-flex gap-2 mb-3" style="max-width:560px;">
  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
  <input type="text" class="form-control" name="fileName" placeholder="review summary file name" required>
  <button class="btn btn-outline-primary text-nowrap" type="submit">Save summary</button>
</form>

<ul class="nav nav-tabs mb-3" role="tablist">
  <li class="nav-item"><button class="nav-link active" data-bs-toggle="tab" data-bs-target="#tab-onboarding" type="button">Onboarding</button></li>
  <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#tab-comp" type="button">Compensation</button></li>
</ul>
<div class="tab-content">
  <div class="tab-pane fade show active" id="tab-onboarding">
    <c:choose>
      <c:when test="${empty tasks}">
        <div class="empty-state">No onboarding tasks.</div>
      </c:when>
      <c:otherwise>
        <table class="table bg-white">
          <thead><tr><th>Task</th><th>Status</th><th>Due</th></tr></thead>
          <tbody>
          <c:forEach var="t" items="${tasks}">
            <tr><td><c:out value="${t.title}"/></td><td><span class="badge-status badge-${fn:toLowerCase(fn:replace(t.status, '_', '-'))}"><c:out value="${t.status}"/></span></td><td><c:out value="${t.dueDate}"/></td></tr>
          </c:forEach>
          </tbody>
        </table>
      </c:otherwise>
    </c:choose>
  </div>
  <div class="tab-pane fade" id="tab-comp">
    <a class="btn btn-sm btn-primary mb-3" href="${pageContext.request.contextPath}/manager/compensation/new?employeeId=${employee.id}">Request compensation change</a>
    <c:choose>
      <c:when test="${empty compensationHistory}">
        <div class="empty-state">No compensation history yet.</div>
      </c:when>
      <c:otherwise>
        <table class="table bg-white">
          <thead><tr><th>Effective</th><th>Salary</th></tr></thead>
          <tbody>
          <c:forEach var="h" items="${compensationHistory}">
            <tr><td><c:out value="${h.effectiveDate}"/></td><td>$<c:out value="${h.salary}"/></td></tr>
          </c:forEach>
          </tbody>
        </table>
      </c:otherwise>
    </c:choose>
  </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
