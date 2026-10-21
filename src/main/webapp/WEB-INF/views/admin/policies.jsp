<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">Policies</h1>

<div class="row g-4">
  <div class="col-lg-7">
    <table class="table bg-white">
      <thead><tr><th>Title</th><th>Version</th><th>Audience</th><th>Active</th></tr></thead>
      <tbody>
      <c:forEach var="p" items="${policies}">
        <tr>
          <td><c:out value="${p.title}"/></td>
          <td>v${p.version}</td>
          <td><c:out value="${p.audience}"/></td>
          <td><c:out value="${p.active}"/></td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
  </div>
  <div class="col-lg-5">
    <div class="card">
      <div class="card-header fw-semibold">Publish new policy</div>
      <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/admin/policies">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <div class="mb-3"><label class="form-label">Title</label><input class="form-control" name="title" required></div>
          <div class="mb-3"><label class="form-label">Audience</label><input class="form-control" name="audience" value="ALL"></div>
          <div class="mb-3"><label class="form-label">Content</label><textarea class="form-control" name="content" rows="4" required></textarea></div>
          <button class="btn btn-primary" type="submit">Publish version 1</button>
        </form>
      </div>
    </div>
  </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
