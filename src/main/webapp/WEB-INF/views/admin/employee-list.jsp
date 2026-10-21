<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">Employees</h1>

<form method="get" class="row g-2 mb-3">
  <div class="col-md-4">
    <input class="form-control" type="search" name="q" value="<c:out value='${q}'/>" placeholder="Search name or email">
  </div>
  <div class="col-md-3">
    <select class="form-select" name="departmentId">
      <option value="">All departments</option>
      <c:forEach var="d" items="${departments}">
        <option value="${d.id}"><c:out value="${d.name}"/></option>
      </c:forEach>
    </select>
  </div>
  <div class="col-md-3">
    <select class="form-select" name="role">
      <option value="">All roles</option>
      <c:forEach var="r" items="${roles}">
        <option value="${r}"><c:out value="${r}"/></option>
      </c:forEach>
    </select>
  </div>
  <div class="col-md-2">
    <button class="btn btn-primary w-100" type="submit">Filter</button>
  </div>
</form>

<c:choose>
  <c:when test="${empty employees}">
    <div class="empty-state">No employees match your filters.</div>
  </c:when>
  <c:otherwise>
    <div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
      <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Department</th><th>Status</th><th class="text-end"></th></tr></thead>
      <tbody>
      <c:forEach var="e" items="${employees}">
        <tr>
          <td><c:out value="${e.fullName}"/></td>
          <td><c:out value="${e.email}"/></td>
          <td><c:out value="${e.role}"/></td>
          <td><c:out value="${e.department.name}"/></td>
          <td><c:out value="${e.employmentStatus}"/></td>
          <td class="text-end"><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/employees/${e.id}">Manage</a></td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
    </div>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
