<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">My documents</h1>

<c:choose>
  <c:when test="${empty documents}">
    <div class="empty-state">No documents on file yet.</div>
  </c:when>
  <c:otherwise>
    <div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
      <thead><tr><th>Category</th><th>File</th><th>Uploaded</th><th class="text-end">Download</th></tr></thead>
      <tbody>
      <c:forEach var="d" items="${documents}">
        <tr>
          <td><c:out value="${d.category}"/></td>
          <td><c:out value="${d.displayName}"/></td>
          <td><c:out value="${d.uploadedAt}"/></td>
          <td class="text-end"><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/documents/${d.id}/download">Download</a></td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
    </div>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
