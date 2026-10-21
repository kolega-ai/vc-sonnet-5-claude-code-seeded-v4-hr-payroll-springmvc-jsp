<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Reset password - HR &amp; Payroll Portal</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body class="d-flex align-items-center" style="min-height:100vh;background:#e7f1fa;">
<div class="container">
  <div class="row justify-content-center">
    <div class="col-md-5">
      <div class="card shadow-sm">
        <div class="card-body p-4">
          <h1 class="h4 fw-bold section-title">Choose a new password</h1>
          <c:if test="${success != null}">
            <c:choose>
              <c:when test="${success}">
                <div class="alert alert-success">Your password was updated. <a href="${pageContext.request.contextPath}/login">Sign in</a>.</div>
              </c:when>
              <c:otherwise>
                <div class="alert alert-danger">That reset link is invalid, expired, or already used.</div>
              </c:otherwise>
            </c:choose>
          </c:if>
          <c:if test="${success == null or !success}">
            <form method="post" action="${pageContext.request.contextPath}/reset-password">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <input type="hidden" name="token" value="<c:out value='${token}'/>"/>
              <div class="mb-3">
                <label class="form-label" for="password">New password</label>
                <input type="password" class="form-control" id="password" name="password" minlength="8" required>
              </div>
              <button class="btn btn-primary w-100" type="submit">Update password</button>
            </form>
          </c:if>
        </div>
      </div>
    </div>
  </div>
</div>
</body>
</html>
