<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">Departments</h1>

<div class="row g-4">
  <div class="col-lg-7">
    <table class="table bg-white">
      <thead><tr><th>Name</th><th>Description</th></tr></thead>
      <tbody>
      <c:forEach var="d" items="${departments}">
        <tr><td><c:out value="${d.name}"/></td><td><c:out value="${d.description}"/></td></tr>
      </c:forEach>
      </tbody>
    </table>
  </div>
  <div class="col-lg-5">
    <div class="card">
      <div class="card-header fw-semibold">Add department</div>
      <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/admin/departments">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <div class="mb-3"><label class="form-label">Name</label><input class="form-control" name="name" required></div>
          <div class="mb-3"><label class="form-label">Description</label><textarea class="form-control" name="description" rows="2"></textarea></div>
          <button class="btn btn-primary" type="submit">Add</button>
        </form>
      </div>
    </div>
  </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
