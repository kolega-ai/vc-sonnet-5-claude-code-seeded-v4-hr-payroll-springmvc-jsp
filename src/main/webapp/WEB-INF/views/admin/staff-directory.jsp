<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">Corporate staff directory</h1>

<div class="card mb-4">
  <div class="card-header fw-semibold">Directory entry</div>
  <div class="card-body">
    <form method="get" action="${pageContext.request.contextPath}/admin/employees/staff-card" class="d-flex gap-2 mb-3">
      <input type="text" class="form-control" name="uid" value="<c:out value='${uid}'/>" placeholder="directory uid" required>
      <button class="btn btn-primary text-nowrap" type="submit">Resolve</button>
    </form>
    <dl class="row mb-0">
      <dt class="col-sm-3">uid</dt><dd class="col-sm-9"><c:out value="${uid}"/></dd>
      <c:forEach var="attr" items="${staffEntry}">
        <dt class="col-sm-3"><c:out value="${attr.key}"/></dt>
        <dd class="col-sm-9"><c:out value="${attr.value}"/></dd>
      </c:forEach>
    </dl>
  </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
