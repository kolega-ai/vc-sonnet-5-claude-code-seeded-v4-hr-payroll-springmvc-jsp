<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<a class="small" href="${pageContext.request.contextPath}/employee/requests">&larr; Back to my requests</a>
<h1 class="section-title h3 mt-2 mb-1"><c:out value="${request.subject}"/></h1>
<p class="text-muted">Status: <c:out value="${request.status}"/></p>
<div class="card mb-4">
  <div class="card-body"><c:out value="${request.description}"/></div>
</div>

<h2 class="h5 section-title">Conversation</h2>
<ul class="list-group mb-4">
  <c:forEach var="c" items="${comments}">
    <li class="list-group-item">
      <div class="small text-muted"><c:out value="${c.author.fullName}"/> &mdash; <c:out value="${c.createdAt}"/></div>
      <div><c:out value="${c.body}"/></div>
    </li>
  </c:forEach>
  <c:if test="${empty comments}">
    <li class="list-group-item text-muted">No messages yet.</li>
  </c:if>
</ul>

<form method="post" action="${pageContext.request.contextPath}/employee/requests/${request.id}/comments">
  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
  <div class="mb-2">
    <textarea class="form-control" name="body" rows="3" required placeholder="Add a reply..."></textarea>
  </div>
  <button class="btn btn-primary" type="submit">Send reply</button>
</form>

<form method="post" action="${pageContext.request.contextPath}/employee/requests/${request.id}/attachments" enctype="multipart/form-data" class="d-flex gap-2 mt-3">
  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
  <input type="file" class="form-control" name="file" required>
  <button class="btn btn-outline-primary text-nowrap" type="submit">Attach file</button>
</form>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
