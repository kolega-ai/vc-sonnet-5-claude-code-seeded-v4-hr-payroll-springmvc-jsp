<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">All HR requests</h1>
<c:choose>
  <c:when test="${empty requests}">
    <div class="empty-state">No HR requests yet.</div>
  </c:when>
  <c:otherwise>
    <table class="table table-hover align-middle bg-white">
      <thead><tr><th>Employee</th><th>Subject</th><th>Status</th><th>Updated</th><th></th></tr></thead>
      <tbody>
      <c:forEach var="r" items="${requests}">
        <tr>
          <td><c:out value="${r.employee.fullName}"/></td>
          <td><c:out value="${r.subject}"/></td>
          <td><span class="badge-status badge-${fn:toLowerCase(fn:replace(r.status, '_', '-'))}"><c:out value="${r.status}"/></span></td>
          <td><c:out value="${r.updatedAt}"/></td>
          <td class="text-end"><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/requests/${r.id}">Open</a></td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
