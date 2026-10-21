<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-1">Welcome, <c:out value="${me.fullName}"/></h1>
<p class="text-muted mb-4"><c:out value="${me.jobTitle}"/></p>

<div class="row g-3 mb-4">
  <div class="col-sm-6 col-lg-3">
    <div class="card summary-card h-100"><div class="card-body">
      <div class="text-muted small">Onboarding tasks</div>
      <div class="value">${tasks.size()}</div>
    </div></div>
  </div>
  <div class="col-sm-6 col-lg-3">
    <div class="card summary-card h-100"><div class="card-body">
      <div class="text-muted small">My documents</div>
      <div class="value">${documents.size()}</div>
    </div></div>
  </div>
  <div class="col-sm-6 col-lg-3">
    <div class="card summary-card h-100"><div class="card-body">
      <div class="text-muted small">HR requests</div>
      <div class="value">${requests.size()}</div>
    </div></div>
  </div>
  <div class="col-sm-6 col-lg-3">
    <div class="card summary-card h-100"><div class="card-body">
      <div class="text-muted small">Active policies</div>
      <div class="value">${activePolicies.size()}</div>
    </div></div>
  </div>
</div>

<div class="row g-4">
  <div class="col-lg-6">
    <div class="card">
      <div class="card-header fw-semibold">My onboarding tasks</div>
      <div class="card-body p-0">
        <c:choose>
          <c:when test="${empty tasks}">
            <div class="empty-state">No onboarding tasks assigned.</div>
          </c:when>
          <c:otherwise>
            <ul class="list-group list-group-flush">
              <c:forEach var="t" items="${tasks}" begin="0" end="5">
                <li class="list-group-item d-flex justify-content-between align-items-center">
                  <span><c:out value="${t.title}"/></span>
                  <span class="badge-status badge-${fn:toLowerCase(fn:replace(t.status, '_', '-'))}"><c:out value="${t.status}"/></span>
                </li>
              </c:forEach>
            </ul>
          </c:otherwise>
        </c:choose>
      </div>
      <div class="card-footer text-end"><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/employee/onboarding">View all</a></div>
    </div>
  </div>
  <div class="col-lg-6">
    <div class="card">
      <div class="card-header fw-semibold">Latest compensation history</div>
      <div class="card-body p-0">
        <c:choose>
          <c:when test="${empty compensationHistory}">
            <div class="empty-state">No compensation history on file yet.</div>
          </c:when>
          <c:otherwise>
            <ul class="list-group list-group-flush">
              <c:forEach var="h" items="${compensationHistory}" begin="0" end="3">
                <li class="list-group-item d-flex justify-content-between align-items-center">
                  <span>Effective <c:out value="${h.effectiveDate}"/></span>
                  <span>$<c:out value="${h.salary}"/></span>
                </li>
              </c:forEach>
            </ul>
          </c:otherwise>
        </c:choose>
      </div>
      <div class="card-footer text-end"><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/employee/compensation">View details</a></div>
    </div>
  </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
