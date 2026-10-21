<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-1">Welcome, <c:out value="${me.fullName}"/></h1>
<p class="text-muted mb-4">Payroll specialist dashboard</p>

<div class="row g-3 mb-4">
  <div class="col-sm-6 col-lg-4">
    <div class="card summary-card h-100"><div class="card-body">
      <div class="text-muted small">Employees</div>
      <div class="value">${employeeCount}</div>
    </div></div>
  </div>
</div>

<div class="card">
  <div class="card-header fw-semibold d-flex justify-content-between align-items-center">
    Recently uploaded documents
    <a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/payroll/documents">Upload document</a>
  </div>
  <div class="card-body p-0">
    <c:choose>
      <c:when test="${empty recentDocuments}">
        <div class="empty-state">No documents uploaded yet.</div>
      </c:when>
      <c:otherwise>
        <table class="table mb-0">
          <thead><tr><th>Employee</th><th>Category</th><th>File</th><th>Uploaded</th></tr></thead>
          <tbody>
          <c:forEach var="d" items="${recentDocuments}">
            <tr>
              <td><c:out value="${d.employee.fullName}"/></td>
              <td><c:out value="${d.category}"/></td>
              <td><c:out value="${d.displayName}"/></td>
              <td><c:out value="${d.uploadedAt}"/></td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </c:otherwise>
    </c:choose>
  </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
