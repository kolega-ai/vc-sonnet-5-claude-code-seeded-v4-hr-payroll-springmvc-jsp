<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">Compensation requests</h1>

<h2 class="h6 fw-semibold">Awaiting my approval</h2>
<c:choose>
  <c:when test="${empty pending}">
    <div class="empty-state">Nothing awaiting your approval.</div>
  </c:when>
  <c:otherwise>
    <table class="table table-hover align-middle bg-white mb-4">
      <thead><tr><th>Employee</th><th>Proposed salary</th><th>Effective</th><th class="text-end">Decision</th></tr></thead>
      <tbody>
      <c:forEach var="r" items="${pending}">
        <tr>
          <td><c:out value="${r.employee.fullName}"/></td>
          <td>$<c:out value="${r.proposedSalary}"/></td>
          <td><c:out value="${r.effectiveDate}"/></td>
          <td class="text-end">
            <form method="post" action="${pageContext.request.contextPath}/manager/compensation/${r.id}/approve" class="d-inline">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <button class="btn btn-sm btn-success" type="submit">Approve</button>
            </form>
            <form method="post" action="${pageContext.request.contextPath}/manager/compensation/${r.id}/reject" class="d-inline">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <input type="hidden" name="reason" value="Not approved at this time"/>
              <button class="btn btn-sm btn-outline-danger" type="submit">Reject</button>
            </form>
          </td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
  </c:otherwise>
</c:choose>

<h2 class="h6 fw-semibold">Requests I submitted</h2>
<c:choose>
  <c:when test="${empty mine}">
    <div class="empty-state">You have not submitted any compensation requests.</div>
  </c:when>
  <c:otherwise>
    <table class="table bg-white">
      <thead><tr><th>Employee</th><th>Salary</th><th>Status</th></tr></thead>
      <tbody>
      <c:forEach var="r" items="${mine}">
        <tr><td><c:out value="${r.employee.fullName}"/></td><td>$<c:out value="${r.proposedSalary}"/></td>
          <td><span class="badge-status badge-${fn:toLowerCase(fn:replace(r.status, '_', '-'))}"><c:out value="${r.status}"/></span></td></tr>
      </c:forEach>
      </tbody>
    </table>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
