<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">Onboarding review queue</h1>
<c:choose>
  <c:when test="${empty tasks}">
    <div class="empty-state">Nothing awaiting your review.</div>
  </c:when>
  <c:otherwise>
    <div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
      <thead><tr><th>Employee</th><th>Task</th><th>Due</th><th class="text-end">Decision</th></tr></thead>
      <tbody>
      <c:forEach var="t" items="${tasks}">
        <tr>
          <td><c:out value="${t.assignee.fullName}"/></td>
          <td><c:out value="${t.title}"/></td>
          <td><c:out value="${t.dueDate}"/></td>
          <td class="text-end">
            <form method="post" action="${pageContext.request.contextPath}/manager/onboarding/${t.id}/approve" class="d-inline">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <button class="btn btn-sm btn-success" type="submit">Approve</button>
            </form>
            <form method="post" action="${pageContext.request.contextPath}/manager/onboarding/${t.id}/reject" class="d-inline">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <input type="hidden" name="notes" value="Needs revision"/>
              <button class="btn btn-sm btn-outline-danger" type="submit">Reject</button>
            </form>
          </td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
    </div>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
