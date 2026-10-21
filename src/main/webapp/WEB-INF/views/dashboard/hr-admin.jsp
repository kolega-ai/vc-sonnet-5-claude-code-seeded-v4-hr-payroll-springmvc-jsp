<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-1">Welcome, <c:out value="${me.fullName}"/></h1>
<p class="text-muted mb-4">HR Admin dashboard</p>

<div class="row g-3 mb-4">
  <div class="col-sm-6 col-lg-3">
    <div class="card summary-card h-100"><div class="card-body">
      <div class="text-muted small">Total employees</div>
      <div class="value">${employeeCount}</div>
    </div></div>
  </div>
  <div class="col-sm-6 col-lg-3">
    <div class="card summary-card h-100"><div class="card-body">
      <div class="text-muted small">Pending HR approvals</div>
      <div class="value">${pendingHrApprovals.size()}</div>
    </div></div>
  </div>
  <div class="col-sm-6 col-lg-3">
    <div class="card summary-card h-100"><div class="card-body">
      <div class="text-muted small">Open HR requests</div>
      <div class="value">${openRequests.size()}</div>
    </div></div>
  </div>
  <div class="col-sm-6 col-lg-3">
    <div class="card summary-card h-100"><div class="card-body">
      <div class="text-muted small">Active policies</div>
      <div class="value">${unacknowledgedPolicies.size()}</div>
    </div></div>
  </div>
</div>

<div class="row g-4">
  <div class="col-lg-6">
    <div class="card">
      <div class="card-header fw-semibold">Compensation requests awaiting HR approval</div>
      <div class="card-body">
        <c:choose>
          <c:when test="${empty pendingHrApprovals}">
            <div class="empty-state">No compensation requests are waiting on HR right now.</div>
          </c:when>
          <c:otherwise>
            <ul class="list-group list-group-flush">
              <c:forEach var="r" items="${pendingHrApprovals}">
                <li class="list-group-item d-flex justify-content-between align-items-center">
                  <span><c:out value="${r.employee.fullName}"/> &mdash; $<c:out value="${r.proposedSalary}"/></span>
                  <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/employees/${r.employee.id}">Review</a>
                </li>
              </c:forEach>
            </ul>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>
  <div class="col-lg-6">
    <div class="card">
      <div class="card-header fw-semibold">Open HR requests</div>
      <div class="card-body">
        <c:choose>
          <c:when test="${empty openRequests}">
            <div class="empty-state">No open HR requests.</div>
          </c:when>
          <c:otherwise>
            <ul class="list-group list-group-flush">
              <c:forEach var="req" items="${openRequests}" begin="0" end="6">
                <li class="list-group-item d-flex justify-content-between align-items-center">
                  <span><c:out value="${req.subject}"/> &mdash; <c:out value="${req.employee.fullName}"/></span>
                  <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/requests/${req.id}">Open</a>
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
