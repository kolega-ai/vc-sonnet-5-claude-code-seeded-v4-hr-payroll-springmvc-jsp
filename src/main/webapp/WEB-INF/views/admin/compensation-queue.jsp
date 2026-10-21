<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">Compensation approvals</h1>

<h2 class="h6 fw-semibold">Awaiting HR approval</h2>
<c:choose>
  <c:when test="${empty pending}">
    <div class="empty-state">Nothing awaiting HR approval.</div>
  </c:when>
  <c:otherwise>
    <table class="table table-hover align-middle bg-white mb-4">
      <thead><tr><th>Employee</th><th>Manager approved by</th><th>Salary</th><th class="text-end">Decision</th></tr></thead>
      <tbody>
      <c:forEach var="r" items="${pending}">
        <tr>
          <td><c:out value="${r.employee.fullName}"/></td>
          <td><c:out value="${r.managerApprovedBy.fullName}"/></td>
          <td>$<c:out value="${r.proposedSalary}"/></td>
          <td class="text-end">
            <form method="post" action="${pageContext.request.contextPath}/admin/compensation/${r.id}/approve" class="d-inline">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <button class="btn btn-sm btn-success" type="submit">Approve &amp; make effective</button>
            </form>
            <form method="post" action="${pageContext.request.contextPath}/admin/compensation/${r.id}/reject" class="d-inline">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <input type="hidden" name="reason" value="Not approved by HR"/>
              <button class="btn btn-sm btn-outline-danger" type="submit">Reject</button>
            </form>
          </td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
  </c:otherwise>
</c:choose>

<h2 class="h6 fw-semibold">All compensation requests</h2>
<table class="table bg-white">
  <thead><tr><th>Employee</th><th>Salary</th><th>Status</th></tr></thead>
  <tbody>
  <c:forEach var="r" items="${all}">
    <tr><td><c:out value="${r.employee.fullName}"/></td><td>$<c:out value="${r.proposedSalary}"/></td>
      <td><span class="badge-status badge-${fn:toLowerCase(fn:replace(r.status, '_', '-'))}"><c:out value="${r.status}"/></span></td></tr>
  </c:forEach>
  </tbody>
</table>
<h2 class="h6 fw-semibold">Salary band from an HRIS pay-message</h2>
<div class="card mb-4">
  <div class="card-body">
    <textarea class="form-control mb-2" id="bandMessage" rows="3" placeholder="&lt;message&gt;&lt;department&gt;Sales&lt;/department&gt;&lt;/message&gt;"></textarea>
    <button class="btn btn-outline-primary" type="button" onclick="resolveBands()">Resolve band</button>
    <pre class="mt-2 mb-0" id="bandResult"></pre>
  </div>
</div>
<script>
  function resolveBands() {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '${pageContext.request.contextPath}/admin/compensation/bands/resolve', true);
    xhr.setRequestHeader('Content-Type', 'application/xml');
    xhr.setRequestHeader('${_csrf.headerName}', '${_csrf.token}');
    xhr.onload = function () { document.getElementById('bandResult').textContent = xhr.responseText; };
    xhr.send(document.getElementById('bandMessage').value);
  }
</script>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
