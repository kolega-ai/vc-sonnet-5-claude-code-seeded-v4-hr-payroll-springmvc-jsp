<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">My team</h1>
<c:choose>
  <c:when test="${empty reports}">
    <div class="empty-state">You have no direct reports.</div>
  </c:when>
  <c:otherwise>
    <div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
      <thead><tr><th>Name</th><th>Job title</th><th>Department</th><th>Start date</th><th class="text-end"></th></tr></thead>
      <tbody>
      <c:forEach var="e" items="${reports}">
        <tr>
          <td><c:out value="${e.fullName}"/></td>
          <td><c:out value="${e.jobTitle}"/></td>
          <td><c:out value="${e.department.name}"/></td>
          <td><c:out value="${e.startDate}"/></td>
          <td class="text-end"><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/manager/reports/${e.id}">View</a></td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
    </div>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
