<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>HR &amp; Payroll Portal</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css?v=${appAssetVersion}">
</head>
<body>
<nav class="navbar navbar-expand-lg navbar-dark app-navbar">
  <div class="container-fluid">
    <a class="navbar-brand fw-semibold" href="${pageContext.request.contextPath}/dashboard">HR &amp; Payroll Portal</a>
    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navMain" aria-controls="navMain" aria-expanded="false" aria-label="Toggle navigation">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navMain">
      <sec:authorize access="isAuthenticated()">
      <ul class="navbar-nav me-auto mb-2 mb-lg-0">
        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
        <sec:authorize access="hasRole('EMPLOYEE')">
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/employee/onboarding">Onboarding</a></li>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/employee/documents">Documents</a></li>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/employee/requests">HR Requests</a></li>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/employee/policies">Policies</a></li>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/employee/compensation">Compensation</a></li>
        </sec:authorize>
        <sec:authorize access="hasRole('MANAGER')">
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/manager/reports">My Team</a></li>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/manager/onboarding">Onboarding Queue</a></li>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/manager/compensation">Compensation</a></li>
        </sec:authorize>
        <sec:authorize access="hasRole('PAYROLL_SPECIALIST')">
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/payroll/documents">Pay Documents</a></li>
        </sec:authorize>
        <sec:authorize access="hasRole('HR_ADMIN')">
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/admin/employees">Employees</a></li>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/admin/compensation">Compensation</a></li>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/admin/departments">Departments</a></li>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/admin/policies">Policies</a></li>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/admin/requests">HR Requests</a></li>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/admin/import-export">Import/Export</a></li>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/admin/audit-log">Audit Log</a></li>
        </sec:authorize>
      </ul>
      <ul class="navbar-nav ms-auto align-items-lg-center">
        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/notifications">Notifications</a></li>
        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/profile">
            <sec:authentication property="name"/></a></li>
        <li class="nav-item">
          <form method="post" action="${pageContext.request.contextPath}/logout" class="d-inline">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <button type="submit" class="btn btn-outline-light btn-sm ms-lg-2">Log out</button>
          </form>
        </li>
      </ul>
      </sec:authorize>
    </div>
  </div>
</nav>
<main class="container-fluid py-4 app-main">
