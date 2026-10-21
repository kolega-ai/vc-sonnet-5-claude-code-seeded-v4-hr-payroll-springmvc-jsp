<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Sign in - HR &amp; Payroll Portal</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body class="d-flex align-items-center" style="min-height:100vh;background:linear-gradient(160deg,#123a5e,#1b5a8a);">
<div class="container">
  <div class="row justify-content-center">
    <div class="col-md-5">
      <div class="card shadow-lg border-0">
        <div class="card-body p-4 p-md-5">
          <h1 class="h4 fw-bold section-title mb-1">HR &amp; Payroll Portal</h1>
          <p class="text-muted mb-4">Sign in to continue</p>

          <c:if test="${param.error != null}">
            <div class="alert alert-danger" role="alert">Invalid email or password.</div>
          </c:if>
          <c:if test="${param.logout != null}">
            <div class="alert alert-success" role="alert">You have been logged out.</div>
          </c:if>

          <form method="post" action="${pageContext.request.contextPath}/login">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <div class="mb-3">
              <label for="username" class="form-label">Work email</label>
              <input type="email" class="form-control" id="username" name="username" required autofocus>
            </div>
            <div class="mb-3">
              <label for="password" class="form-label">Password</label>
              <input type="password" class="form-control" id="password" name="password" required>
            </div>
            <button type="submit" class="btn btn-primary w-100">Sign in</button>
          </form>
          <div class="text-center mt-3">
            <a href="${pageContext.request.contextPath}/forgot-password">Forgot password?</a>
          </div>
          <hr>
          <p class="small text-muted mb-1">Demo accounts (password: <code>Password123!</code>):</p>
          <ul class="small text-muted mb-0">
            <li>hradmin@hrpayroll.example (HR Admin)</li>
            <li>manager@hrpayroll.example (Manager)</li>
            <li>payroll@hrpayroll.example (Payroll Specialist)</li>
            <li>employee@hrpayroll.example (Employee)</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</div>
</body>
</html>
