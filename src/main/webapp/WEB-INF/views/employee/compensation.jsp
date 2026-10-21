<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">My compensation</h1>

<div class="row g-4">
  <div class="col-lg-6">
    <div class="card">
      <div class="card-header fw-semibold">Effective salary history (immutable)</div>
      <div class="card-body p-0">
        <c:choose>
          <c:when test="${empty history}">
            <div class="empty-state">No compensation has been recorded yet.</div>
          </c:when>
          <c:otherwise>
            <table class="table mb-0">
              <thead><tr><th>Effective</th><th>Salary</th><th>Bonus</th></tr></thead>
              <tbody>
              <c:forEach var="h" items="${history}">
                <tr><td><c:out value="${h.effectiveDate}"/></td><td>$<c:out value="${h.salary}"/></td><td>$<c:out value="${h.bonus}"/></td></tr>
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
      <div class="card-header fw-semibold">Compensation change requests</div>
      <div class="card-body p-0">
        <c:choose>
          <c:when test="${empty requests}">
            <div class="empty-state">No compensation change requests on file. Ask your manager to submit one.</div>
          </c:when>
          <c:otherwise>
            <ul class="list-group list-group-flush">
              <c:forEach var="r" items="${requests}">
                <li class="list-group-item d-flex justify-content-between align-items-center">
                  <span>$<c:out value="${r.proposedSalary}"/> from <c:out value="${r.effectiveDate}"/></span>
                  <span class="badge-status badge-${fn:toLowerCase(fn:replace(r.status, '_', '-'))}"><c:out value="${r.status}"/></span>
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
