<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">Notifications</h1>
<c:choose>
  <c:when test="${empty notifications}">
    <div class="empty-state">You're all caught up. No notifications yet.</div>
  </c:when>
  <c:otherwise>
    <ul class="list-group">
      <c:forEach var="n" items="${notifications}">
        <li class="list-group-item d-flex justify-content-between align-items-center ${n.read ? '' : 'fw-semibold'}">
          <span><c:out value="${n.message}"/> <span class="text-muted small">&mdash; <c:out value="${n.createdAt}"/></span></span>
          <c:if test="${!n.read}">
            <form method="post" action="${pageContext.request.contextPath}/notifications/${n.id}/read">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <button class="btn btn-sm btn-outline-secondary" type="submit">Mark read</button>
            </form>
          </c:if>
        </li>
      </c:forEach>
    </ul>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
