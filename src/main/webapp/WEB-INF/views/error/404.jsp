<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="text-center py-5">
  <h1 class="display-6 fw-bold">404 &mdash; Not found</h1>
  <p class="text-muted"><c:out value="${message}"/></p>
  <a class="btn btn-primary" href="${pageContext.request.contextPath}/dashboard">Back to dashboard</a>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
