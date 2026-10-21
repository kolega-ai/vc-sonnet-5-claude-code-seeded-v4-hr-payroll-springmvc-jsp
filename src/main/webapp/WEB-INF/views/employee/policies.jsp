<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">Company policies</h1>

<c:choose>
  <c:when test="${empty policies}">
    <div class="empty-state">No active policies.</div>
  </c:when>
  <c:otherwise>
    <div class="row g-3">
    <c:forEach var="p" items="${policies}">
      <c:set var="acked" value="false"/>
      <c:forEach var="a" items="${acks}">
        <c:if test="${a.policy.id == p.id and a.policyVersion >= p.version}"><c:set var="acked" value="true"/></c:if>
      </c:forEach>
      <div class="col-md-6">
        <div class="card h-100">
          <div class="card-body">
            <h2 class="h6 fw-semibold"><c:out value="${p.title}"/> <span class="text-muted small">v${p.version}</span></h2>
            <p class="small text-muted"><c:out value="${p.content}"/></p>
            <c:choose>
              <c:when test="${acked}">
                <span class="badge-status badge-approved">&#10003; Acknowledged</span>
              </c:when>
              <c:otherwise>
                <form method="post" action="${pageContext.request.contextPath}/employee/policies/${p.id}/acknowledge">
                  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                  <button class="btn btn-sm btn-primary" type="submit">Acknowledge</button>
                </form>
              </c:otherwise>
            </c:choose>
          </div>
        </div>
      </div>
    </c:forEach>
    </div>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
