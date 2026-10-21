<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="text-center py-5">
  <h1 class="display-6 fw-bold text-danger">500 &mdash; Something went wrong</h1>
  <p class="text-muted"><c:out value="${message}"/></p>
  <pre class="text-start small bg-light p-3 border rounded"><c:out value="${detail}"/></pre>
  <a class="btn btn-primary" href="${pageContext.request.contextPath}/dashboard">Back to dashboard</a>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
