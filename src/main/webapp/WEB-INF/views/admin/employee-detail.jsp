<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<a class="small" href="${pageContext.request.contextPath}/admin/employees">&larr; Back to employees</a>
<h1 class="section-title h3 mt-2 mb-1"><c:out value="${employee.fullName}"/></h1>
<p class="text-muted"><c:out value="${employee.role}"/> &mdash; <c:out value="${employee.email}"/></p>
<c:if test="${not empty orgChangeRef}">
  <div class="alert alert-success">Reporting line updated for org change <c:out value="${orgChangeRef}"/>.</div>
</c:if>

<ul class="nav nav-tabs mb-3">
  <li class="nav-item"><button class="nav-link active" data-bs-toggle="tab" data-bs-target="#tab-profile" type="button">Profile</button></li>
  <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#tab-comp" type="button">Compensation</button></li>
  <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#tab-tasks" type="button">Onboarding</button></li>
  <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#tab-docs" type="button">Documents</button></li>
  <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#tab-requests" type="button">HR requests</button></li>
  <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#tab-role" type="button">Role &amp; access</button></li>
</ul>

<div class="tab-content">
  <div class="tab-pane fade show active" id="tab-profile">
    <form method="post" action="${pageContext.request.contextPath}/admin/employees/${employee.id}" class="row g-3" style="max-width:720px;">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
      <div class="col-md-6">
        <label class="form-label">Full name</label>
        <input class="form-control" name="fullName" value="<c:out value='${form.fullName}'/>" required>
      </div>
      <div class="col-md-6">
        <label class="form-label">Job title</label>
        <input class="form-control" name="jobTitle" value="<c:out value='${form.jobTitle}'/>">
      </div>
      <div class="col-md-6">
        <label class="form-label">Department</label>
        <select class="form-select" name="departmentId" required>
          <c:forEach var="d" items="${departments}">
            <option value="${d.id}" ${d.id == form.departmentId ? 'selected' : ''}><c:out value="${d.name}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="col-md-6">
        <label class="form-label">Manager</label>
        <select class="form-select" name="managerId">
          <option value="">None</option>
          <c:forEach var="m" items="${managers}">
            <option value="${m.id}" ${m.id == form.managerId ? 'selected' : ''}><c:out value="${m.fullName}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="col-md-6">
        <label class="form-label">Employment status</label>
        <select class="form-select" name="employmentStatus" required>
          <option value="ACTIVE" ${form.employmentStatus == 'ACTIVE' ? 'selected' : ''}>Active</option>
          <option value="ON_LEAVE" ${form.employmentStatus == 'ON_LEAVE' ? 'selected' : ''}>On leave</option>
          <option value="TERMINATED" ${form.employmentStatus == 'TERMINATED' ? 'selected' : ''}>Terminated</option>
        </select>
      </div>
      <div class="col-md-6">
        <label class="form-label">Start date</label>
        <input type="date" class="form-control" name="startDate" value="${form.startDate}">
      </div>
      <div class="col-md-6">
        <label class="form-label">Phone</label>
        <input class="form-control" name="phone" value="<c:out value='${form.phone}'/>">
      </div>
      <div class="col-md-6">
        <label class="form-label">Address</label>
        <input class="form-control" name="address" value="<c:out value='${form.address}'/>">
      </div>
      <div class="col-12">
        <button class="btn btn-primary" type="submit">Save profile</button>
      </div>
    </form>
  </div>

  <div class="tab-pane fade" id="tab-comp">
    <h2 class="h6">Immutable salary history</h2>
    <table class="table bg-white mb-4">
      <thead><tr><th>Effective</th><th>Salary</th><th>Bonus</th></tr></thead>
      <tbody>
      <c:forEach var="h" items="${compensationHistory}">
        <tr><td><c:out value="${h.effectiveDate}"/></td><td>$<c:out value="${h.salary}"/></td><td>$<c:out value="${h.bonus}"/></td></tr>
      </c:forEach>
      </tbody>
    </table>
    <h2 class="h6">Requests</h2>
    <table class="table bg-white">
      <thead><tr><th>Salary</th><th>Status</th></tr></thead>
      <tbody>
      <c:forEach var="r" items="${compensationRequests}">
        <tr><td>$<c:out value="${r.proposedSalary}"/></td><td><span class="badge-status badge-${fn:toLowerCase(fn:replace(r.status, '_', '-'))}"><c:out value="${r.status}"/></span></td></tr>
      </c:forEach>
      </tbody>
    </table>
  </div>

  <div class="tab-pane fade" id="tab-tasks">
    <table class="table bg-white">
      <thead><tr><th>Task</th><th>Status</th></tr></thead>
      <tbody>
      <c:forEach var="t" items="${tasks}">
        <tr><td><c:out value="${t.title}"/></td><td><span class="badge-status badge-${fn:toLowerCase(fn:replace(t.status, '_', '-'))}"><c:out value="${t.status}"/></span></td></tr>
      </c:forEach>
      </tbody>
    </table>
  </div>

  <div class="tab-pane fade" id="tab-docs">
    <table class="table bg-white">
      <thead><tr><th>Category</th><th>File</th><th></th></tr></thead>
      <tbody>
      <c:forEach var="d" items="${documents}">
        <tr><td><c:out value="${d.category}"/></td><td><c:out value="${d.displayName}"/></td>
          <td><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/documents/${d.id}/download">Download</a></td></tr>
      </c:forEach>
      </tbody>
    </table>
  </div>

  <div class="tab-pane fade" id="tab-requests">
    <table class="table bg-white">
      <thead><tr><th>Subject</th><th>Status</th><th></th></tr></thead>
      <tbody>
      <c:forEach var="r" items="${requests}">
        <tr><td><c:out value="${r.subject}"/></td><td><c:out value="${r.status}"/></td>
          <td><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/requests/${r.id}">Open</a></td></tr>
      </c:forEach>
      </tbody>
    </table>
  </div>

  <div class="tab-pane fade" id="tab-role">
    <form method="post" action="${pageContext.request.contextPath}/admin/employees/${employee.id}/role" class="row g-2 mb-4" style="max-width:420px;">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
      <div class="col-8">
        <select class="form-select" name="role">
          <option value="HR_ADMIN" ${employee.role == 'HR_ADMIN' ? 'selected' : ''}>HR Admin</option>
          <option value="MANAGER" ${employee.role == 'MANAGER' ? 'selected' : ''}>Manager</option>
          <option value="PAYROLL_SPECIALIST" ${employee.role == 'PAYROLL_SPECIALIST' ? 'selected' : ''}>Payroll Specialist</option>
          <option value="EMPLOYEE" ${employee.role == 'EMPLOYEE' ? 'selected' : ''}>Employee</option>
        </select>
      </div>
      <div class="col-4"><button class="btn btn-outline-primary w-100" type="submit">Change role</button></div>
    </form>
    <form method="post" action="${pageContext.request.contextPath}/admin/employees/${employee.id}/manager" class="row g-2 mb-4" style="max-width:420px;">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
      <div class="col-8">
        <select class="form-select" name="managerId">
          <c:forEach var="m" items="${managers}">
            <option value="${m.id}"><c:out value="${m.fullName}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="col-4"><button class="btn btn-outline-primary w-100" type="submit">Reassign manager</button></div>
    </form>
    <div class="card mb-4" style="max-width:640px;">
      <div class="card-header fw-semibold">Portal access recovery</div>
      <div class="card-body">
        <c:if test="${not empty recoveryLink}">
          <div class="alert alert-warning">Recovery address for this employee:
            <code><c:out value="${recoveryLink}"/></code>
          </div>
        </c:if>
        <c:if test="${tempAccessIssued}">
          <div class="alert alert-success">A temporary access code was mailed to this employee.</div>
        </c:if>
        <div class="d-flex gap-2">
          <form method="post" action="${pageContext.request.contextPath}/admin/employees/${employee.id}/recovery-link">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <button class="btn btn-outline-secondary" type="submit">Generate recovery address</button>
          </form>
          <form method="post" action="${pageContext.request.contextPath}/admin/employees/${employee.id}/temp-access">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <button class="btn btn-outline-secondary" type="submit">Issue temporary access code</button>
          </form>
        </div>
      </div>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/admin/employees/${employee.id}/enabled">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
      <c:choose>
        <c:when test="${employee.enabled}">
          <input type="hidden" name="enabled" value="false"/>
          <button class="btn btn-outline-danger" type="submit">Disable account</button>
        </c:when>
        <c:otherwise>
          <input type="hidden" name="enabled" value="true"/>
          <button class="btn btn-outline-success" type="submit">Enable account</button>
        </c:otherwise>
      </c:choose>
    </form>
  </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
