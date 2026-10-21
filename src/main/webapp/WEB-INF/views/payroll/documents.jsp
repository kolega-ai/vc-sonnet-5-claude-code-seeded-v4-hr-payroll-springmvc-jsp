<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">Payroll document center</h1>

<c:if test="${not empty docTotals}">
  <div class="card mb-4">
    <div class="card-header fw-semibold">Totals in <c:out value="${totalsFor.displayName}"/></div>
    <div class="card-body">
      <dl class="row mb-0">
        <c:forEach var="t" items="${docTotals}">
          <dt class="col-sm-4"><c:out value="${t.key}"/></dt><dd class="col-sm-8"><c:out value="${t.value}"/></dd>
        </c:forEach>
      </dl>
    </div>
  </div>
</c:if>

<c:if test="${not empty batchFiles}">
  <div class="card mb-4">
    <div class="card-header fw-semibold">Files in the uploaded batch</div>
    <div class="card-body">
      <ul class="mb-0">
        <c:forEach var="bf" items="${batchFiles}"><li><c:out value="${bf}"/></li></c:forEach>
      </ul>
    </div>
  </div>
</c:if>

<c:if test="${not empty extractedText}">
  <div class="card mb-4">
    <div class="card-header fw-semibold">Extracted text from <c:out value="${extractFor.displayName}"/></div>
    <div class="card-body"><pre class="mb-0"><c:out value="${extractedText}"/></pre></div>
  </div>
</c:if>
<c:if test="${not empty queryResult}">
  <div class="alert alert-info">Matching total: <c:out value="${queryResult}"/></div>
</c:if>

<div class="row g-4">
  <div class="col-lg-5">
    <div class="card">
      <div class="card-header fw-semibold">Upload document</div>
      <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/payroll/documents" enctype="multipart/form-data">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <div class="mb-3">
            <label class="form-label">Employee</label>
            <select class="form-select" name="employeeId" required>
              <c:forEach var="e" items="${employees}">
                <option value="${e.id}"><c:out value="${e.fullName}"/></option>
              </c:forEach>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-label">Category</label>
            <select class="form-select" name="category" required>
              <c:forEach var="cat" items="${categories}">
                <option value="${cat}"><c:out value="${cat}"/></option>
              </c:forEach>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-label">File</label>
            <input type="file" class="form-control" name="file" required>
          </div>
          <button class="btn btn-primary" type="submit">Upload</button>
        </form>
        <hr>
        <p class="small text-muted">Preview a batch the exchange delivered: drop the manifest that names the batch folder.</p>
        <form method="post" action="${pageContext.request.contextPath}/payroll/documents/batch-preview" enctype="multipart/form-data" class="d-flex gap-2">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <input type="file" name="manifest" class="form-control" required>
          <button class="btn btn-outline-primary text-nowrap" type="submit">Preview batch</button>
        </form>
        <hr>
        <p class="small text-muted">Read the text of a stored document with the desk's OCR helper.</p>
        <form method="post" action="${pageContext.request.contextPath}/payroll/documents/extract" class="d-flex gap-2">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <input type="number" class="form-control" name="id" placeholder="document id" required>
          <select class="form-select" name="lang">
            <option value="eng">English</option>
            <option value="fra">French</option>
            <option value="deu">German</option>
          </select>
          <button class="btn btn-outline-primary text-nowrap" type="submit">Extract text</button>
        </form>
        <hr>
        <p class="small text-muted">Look up a named total in a stored payslip using the desk's query file.</p>
        <form method="post" action="${pageContext.request.contextPath}/payroll/documents/totals/query" enctype="multipart/form-data" class="d-flex gap-2">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <input type="number" class="form-control" name="id" placeholder="document id" required>
          <input type="file" name="query" class="form-control" required>
          <button class="btn btn-outline-primary text-nowrap" type="submit">Query total</button>
        </form>
      </div>
    </div>
  </div>
  <div class="col-lg-7">
    <div class="input-group mb-3">
      <span class="input-group-text">Printer</span>
      <input type="text" class="form-control" id="printQueue" placeholder="queue name">
    </div>
    <div class="card">
      <div class="card-header fw-semibold">All documents</div>
      <div class="card-body p-0">
        <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead><tr><th>Employee</th><th>Category</th><th>File</th><th></th></tr></thead>
          <tbody>
          <c:forEach var="d" items="${documents}">
            <tr>
              <td><c:out value="${d.employee.fullName}"/></td>
              <td><c:out value="${d.category}"/></td>
              <td><c:out value="${d.displayName}"/></td>
              <td class="text-nowrap">
                <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/documents/${d.id}/download">Download</a>
                <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/payroll/documents/${d.id}/totals">Totals</a>
                <button class="btn btn-sm btn-outline-secondary" type="button" onclick="printDoc(${d.id})">Print</button>
              </td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
        </div>
      </div>
    </div>
  </div>
</div>
<script>
  function printDoc(id) {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '${pageContext.request.contextPath}/payroll/documents/' + id + '/print', true);
    xhr.setRequestHeader('X-Print-Queue', document.getElementById('printQueue').value);
    xhr.setRequestHeader('${_csrf.headerName}', '${_csrf.token}');
    xhr.onload = function () { location.href = '${pageContext.request.contextPath}/payroll/documents'; };
    xhr.send();
  }
</script>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
