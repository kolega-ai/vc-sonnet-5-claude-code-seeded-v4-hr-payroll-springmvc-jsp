<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<a class="small" href="${pageContext.request.contextPath}/admin/requests">&larr; Back to HR requests</a>
<h1 class="section-title h3 mt-2 mb-1"><c:out value="${request.subject}"/></h1>
<p class="text-muted">From <c:out value="${request.employee.fullName}"/> &mdash; status <c:out value="${request.status}"/></p>
<div class="card mb-4"><div class="card-body"><c:out value="${request.description}"/></div></div>

<form method="post" action="${pageContext.request.contextPath}/admin/requests/${request.id}/status" class="d-flex gap-2 mb-4" style="max-width:420px;">
  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
  <select class="form-select" name="status">
    <option value="OPEN">Open</option>
    <option value="WAITING_ON_EMPLOYEE">Waiting on employee</option>
    <option value="WAITING_ON_HR">Waiting on HR</option>
    <option value="RESOLVED">Resolved</option>
    <option value="CLOSED">Closed</option>
  </select>
  <button class="btn btn-outline-primary text-nowrap" type="submit">Update status</button>
</form>

<h2 class="h5 section-title">Conversation (including internal notes)</h2>
<ul class="list-group mb-4">
  <c:forEach var="c" items="${comments}">
    <li class="list-group-item ${c.internalOnly ? 'bg-warning-subtle' : ''}">
      <div class="small text-muted"><c:out value="${c.author.fullName}"/> &mdash; <c:out value="${c.createdAt}"/>
        <c:if test="${c.internalOnly}"> &mdash; <strong>Internal only</strong></c:if>
      </div>
      <div>${c.body}</div>
    </li>
  </c:forEach>
</ul>

<form method="post" action="${pageContext.request.contextPath}/admin/requests/${request.id}/comments">
  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
  <div class="mb-2"><textarea class="form-control" name="body" rows="3" required placeholder="Reply to employee or add an internal note..."></textarea></div>
  <div class="form-check mb-2">
    <input class="form-check-input" type="checkbox" name="internalOnly" value="true" id="internalOnly">
    <label class="form-check-label" for="internalOnly">Internal note (hidden from employee)</label>
  </div>
  <button class="btn btn-primary" type="submit">Post</button>
</form>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
