<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">My HR requests</h1>

<div class="row g-4">
  <div class="col-lg-7">
    <c:choose>
      <c:when test="${empty requests}">
        <div class="empty-state">You have not submitted any HR requests yet.</div>
      </c:when>
      <c:otherwise>
        <div class="table-responsive">
        <table class="table table-hover align-middle bg-white">
          <thead><tr><th>Subject</th><th>Status</th><th>Updated</th><th></th></tr></thead>
          <tbody>
          <c:forEach var="r" items="${requests}">
            <tr>
              <td><c:out value="${r.subject}"/></td>
              <td><span class="badge-status badge-${fn:toLowerCase(fn:replace(r.status, '_', '-'))}"><c:out value="${r.status}"/></span></td>
              <td><c:out value="${r.updatedAt}"/></td>
              <td class="text-end"><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/employee/requests/${r.id}">Open</a></td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
  <div class="col-lg-5">
    <div class="card">
      <div class="card-header fw-semibold">New HR request</div>
      <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/employee/requests">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <div class="mb-3">
            <label class="form-label" for="subject">Subject</label>
            <input class="form-control" id="subject" name="subject" required>
          </div>
          <div class="mb-3">
            <label class="form-label" for="description">Description</label>
            <textarea class="form-control" id="description" name="description" rows="4" required></textarea>
          </div>
          <button type="submit" class="btn btn-primary">Submit request</button>
        </form>
      </div>
    </div>
  </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
