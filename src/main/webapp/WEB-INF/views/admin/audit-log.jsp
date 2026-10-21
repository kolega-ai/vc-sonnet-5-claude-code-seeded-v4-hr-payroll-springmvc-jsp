<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">Audit log</h1>
<p class="text-muted">Append-only record of privileged actions. Entries can never be edited or deleted.</p>

<c:choose>
  <c:when test="${empty events.content}">
    <div class="empty-state">No audit events yet.</div>
  </c:when>
  <c:otherwise>
    <div class="table-responsive">
    <table class="table table-sm bg-white">
      <thead><tr><th>Time</th><th>Actor</th><th>Action</th><th>Resource</th><th>Detail</th><th>Basis</th></tr></thead>
      <tbody>
      <c:forEach var="ev" items="${events.content}">
        <tr>
          <td class="text-nowrap"><c:out value="${ev.timestamp}"/></td>
          <td><c:out value="${ev.actorEmail}"/></td>
          <td><c:out value="${ev.action}"/></td>
          <td><c:out value="${ev.resourceType}"/> <c:out value="${ev.resourceId}"/></td>
          <td class="small"><c:out value="${ev.afterValue}"/></td>
          <td class="small text-muted"><c:out value="${ev.authorizationBasis}"/></td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
    </div>
    <nav>
      <ul class="pagination">
        <c:if test="${events.number > 0}">
          <li class="page-item"><a class="page-link" href="?page=${events.number - 1}">Previous</a></li>
        </c:if>
        <c:if test="${events.hasNext()}">
          <li class="page-item"><a class="page-link" href="?page=${events.number + 1}">Next</a></li>
        </c:if>
      </ul>
    </nav>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
