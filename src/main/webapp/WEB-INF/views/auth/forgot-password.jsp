<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Forgot password - HR &amp; Payroll Portal</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body class="d-flex align-items-center" style="min-height:100vh;background:#e7f1fa;">
<div class="container">
  <div class="row justify-content-center">
    <div class="col-md-5">
      <div class="card shadow-sm">
        <div class="card-body p-4">
          <h1 class="h4 fw-bold section-title">Reset your password</h1>
          <c:choose>
            <c:when test="${submitted}">
              <div class="alert alert-success">If that email exists, a reset link has been sent (check the mock outbox / logs for the demo link).</div>
              <a class="btn btn-outline-primary w-100" href="${pageContext.request.contextPath}/login">Back to sign in</a>
            </c:when>
            <c:otherwise>
              <p class="text-muted">Enter your account email and we'll email you a single-use reset link, valid for one hour.</p>
              <form method="post" action="${pageContext.request.contextPath}/forgot-password">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <div class="mb-3">
                  <label class="form-label" for="email">Email</label>
                  <input type="email" class="form-control" id="email" name="email" required>
                </div>
                <button class="btn btn-primary w-100" type="submit">Send reset link</button>
              </form>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </div>
</div>
</body>
</html>
