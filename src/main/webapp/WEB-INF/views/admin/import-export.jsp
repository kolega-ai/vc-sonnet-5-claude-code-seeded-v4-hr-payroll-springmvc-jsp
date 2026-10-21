<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">Import &amp; export</h1>

<c:if test="${not empty importResult}">
  <div class="alert alert-success"><c:out value="${importResult}"/></div>
</c:if>
<c:if test="${not empty feedRouteResult}">
  <div class="alert alert-info"><c:out value="${feedRouteResult}"/></div>
</c:if>

<div class="row g-4">
  <div class="col-lg-6">
    <div class="card mb-4">
      <div class="card-header fw-semibold">Import departments (CSV)</div>
      <div class="card-body">
        <p class="small text-muted">Header: <code>name,description</code></p>
        <form method="post" action="${pageContext.request.contextPath}/admin/import/departments" enctype="multipart/form-data" class="d-flex gap-2">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <input type="file" name="file" class="form-control" accept=".csv" required>
          <button class="btn btn-primary text-nowrap" type="submit">Import</button>
        </form>
      </div>
    </div>
    <div class="card">
      <div class="card-header fw-semibold">Import employees (CSV)</div>
      <div class="card-body">
        <p class="small text-muted">Header: <code>email,fullName,role,department,jobTitle,startDate,phone,address</code></p>
        <form method="post" action="${pageContext.request.contextPath}/admin/import/employees" enctype="multipart/form-data" class="d-flex gap-2">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <input type="file" name="file" class="form-control" accept=".csv" required>
          <button class="btn btn-primary text-nowrap" type="submit">Import</button>
        </form>
      </div>
    </div>
  </div>
  <div class="col-lg-6">
    <div class="card mb-4">
      <div class="card-header fw-semibold">Payroll feed</div>
      <div class="card-body">
        <p class="small text-muted">Load the connection profile issued by the HRIS vendor (<code>.properties</code>).</p>
        <form method="post" action="${pageContext.request.contextPath}/admin/import/feed-profile" enctype="multipart/form-data" class="d-flex gap-2 mb-3">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <input type="file" name="file" class="form-control" accept=".properties" required>
          <button class="btn btn-primary text-nowrap" type="submit">Load profile</button>
        </form>
        <form method="post" action="${pageContext.request.contextPath}/admin/import/feed-route-check">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <button class="btn btn-outline-secondary" type="submit">Check callback route</button>
        </form>
        <div class="d-flex gap-2 mt-2">
          <input type="text" class="form-control" id="deskRef" placeholder="desk reference">
          <button class="btn btn-outline-secondary text-nowrap" type="button" onclick="announceInterchange()">Announce interchange</button>
        </div>
      </div>
    </div>
    <div class="card mb-4">
      <div class="card-header fw-semibold">Legacy feed profile (XML)</div>
      <div class="card-body">
        <p class="small text-muted">For bureaus still on the vendor's older tooling: paste the XML profile document it emitted.</p>
        <form method="get" action="${pageContext.request.contextPath}/admin/import/feed-legacy">
          <textarea class="form-control mb-2" name="definition" rows="3" placeholder="&lt;hrFeed&gt;...&lt;/hrFeed&gt;" required></textarea>
          <button class="btn btn-primary" type="submit">Load legacy profile</button>
        </form>
      </div>
    </div>
    <div class="card mb-4">
      <div class="card-header fw-semibold">Reporting lines (CSV)</div>
      <div class="card-body">
        <p class="small text-muted">Header: <code>employeeEmail,managerEmail</code> &mdash; from the HRIS org-change export.</p>
        <form method="post" action="${pageContext.request.contextPath}/admin/import/manager-map" enctype="multipart/form-data" class="d-flex gap-2">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <input type="file" name="file" class="form-control" accept=".csv" required>
          <button class="btn btn-primary text-nowrap" type="submit">Apply</button>
        </form>
      </div>
    </div>
    <div class="card mb-4">
      <div class="card-header fw-semibold">Void compensation requests (CSV)</div>
      <div class="card-body">
        <p class="small text-muted">Header: <code>requestId,reason</code> &mdash; from the bureau run sheet.</p>
        <form method="post" action="${pageContext.request.contextPath}/admin/import/compensation-voids" enctype="multipart/form-data" class="d-flex gap-2">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <input type="file" name="file" class="form-control" accept=".csv" required>
          <button class="btn btn-primary text-nowrap" type="submit">Void</button>
        </form>
      </div>
    </div>
    <div class="card">
      <div class="card-header fw-semibold">Exports</div>
      <div class="card-body d-grid gap-2">
        <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/admin/export/payroll">Export payroll (CSV)</a>
        <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/admin/export/onboarding">Export onboarding status (CSV)</a>
        <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/admin/export/policy-acknowledgements">Export policy acknowledgements (CSV)</a>
        <div class="input-group mt-2">
          <input type="text" class="form-control" id="runRef" placeholder="run reference">
          <button class="btn btn-outline-primary" type="button" onclick="location.href='${pageContext.request.contextPath}/admin/export/payroll-run?runRef='+encodeURIComponent(document.getElementById('runRef').value)">Export run file</button>
        </div>
        <a class="btn btn-outline-secondary mt-2" href="${pageContext.request.contextPath}/admin/employees/staff-card">Corporate staff directory</a>
      </div>
    </div>
  </div>
</div>
<script>
  function announceInterchange() {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '${pageContext.request.contextPath}/admin/import/feed-interchange', true);
    xhr.setRequestHeader('X-Desk-Ref', document.getElementById('deskRef').value);
    xhr.setRequestHeader('${_csrf.headerName}', '${_csrf.token}');
    xhr.onload = function () { location.href = '${pageContext.request.contextPath}/admin/import-export'; };
    xhr.send();
  }
</script>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
