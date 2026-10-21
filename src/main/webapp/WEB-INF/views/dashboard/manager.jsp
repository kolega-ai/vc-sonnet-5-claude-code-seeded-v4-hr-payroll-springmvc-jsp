<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-1">Welcome, <c:out value="${me.fullName}"/></h1>
<p class="text-muted mb-4">Manager dashboard</p>

<div class="row g-3 mb-4">
  <div class="col-sm-6 col-lg-4">
    <div class="card summary-card h-100"><div class="card-body">
      <div class="text-muted small">Direct reports</div>
      <div class="value">${directReports.size()}</div>
    </div></div>
  </div>
  <div class="col-sm-6 col-lg-4">
    <div class="card summary-card h-100"><div class="card-body">
      <div class="text-muted small">Compensation requests to approve</div>
      <div class="value">${pendingApprovals.size()}</div>
    </div></div>
  </div>
  <div class="col-sm-6 col-lg-4">
    <div class="card summary-card h-100"><div class="card-body">
      <div class="text-muted small">Onboarding tasks to review</div>
      <div class="value">${pendingOnboarding.size()}</div>
    </div></div>
  </div>
</div>

<div class="row g-4">
  <div class="col-lg-6">
    <div class="card">
      <div class="card-header fw-semibold">My direct reports</div>
      <div class="card-body p-0">
        <c:choose>
          <c:when test="${empty directReports}">
            <div class="empty-state">No direct reports yet.</div>
          </c:when>
          <c:otherwise>
            <table class="table mb-0">
              <thead><tr><th>Name</th><th>Job title</th><th></th></tr></thead>
              <tbody>
              <c:forEach var="e" items="${directReports}">
                <tr>
                  <td><c:out value="${e.fullName}"/></td>
                  <td><c:out value="${e.jobTitle}"/></td>
                  <td class="text-end"><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/manager/reports/${e.id}">View</a></td>
                </tr>
              </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>
  <div class="col-lg-6">
    <div class="card">
      <div class="card-header fw-semibold">Pending compensation approvals</div>
      <div class="card-body">
        <c:choose>
          <c:when test="${empty pendingApprovals}">
            <div class="empty-state">Nothing pending your approval.</div>
          </c:when>
          <c:otherwise>
            <ul class="list-group list-group-flush">
              <c:forEach var="r" items="${pendingApprovals}">
                <li class="list-group-item d-flex justify-content-between align-items-center">
                  <span><c:out value="${r.employee.fullName}"/></span>
                  <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/manager/compensation">Review</a>
                </li>
              </c:forEach>
            </ul>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
