<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">My profile</h1>

<c:if test="${success}">
  <div class="alert alert-success">Your profile was updated.</div>
</c:if>

<div class="row g-4">
  <div class="col-lg-5">
    <div class="card">
      <div class="card-header fw-semibold">Employment details</div>
      <div class="card-body">
        <dl class="row mb-0">
          <dt class="col-sm-5">Full name</dt><dd class="col-sm-7"><c:out value="${me.fullName}"/></dd>
          <dt class="col-sm-5">Email</dt><dd class="col-sm-7"><c:out value="${me.email}"/></dd>
          <dt class="col-sm-5">Job title</dt><dd class="col-sm-7"><c:out value="${me.jobTitle}"/></dd>
          <dt class="col-sm-5">Department</dt><dd class="col-sm-7"><c:out value="${me.department.name}"/></dd>
          <dt class="col-sm-5">Manager</dt><dd class="col-sm-7"><c:out value="${me.manager.fullName}"/></dd>
          <dt class="col-sm-5">Start date</dt><dd class="col-sm-7"><c:out value="${me.startDate}"/></dd>
          <dt class="col-sm-5">Status</dt><dd class="col-sm-7"><c:out value="${me.employmentStatus}"/></dd>
        </dl>
        <p class="small text-muted mt-3 mb-0">To change employment details (name, department, manager, status), contact HR.</p>
      </div>
    </div>
  </div>
  <div class="col-lg-7">
    <div class="card">
      <div class="card-header fw-semibold">Contact information</div>
      <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/profile">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <div class="mb-3">
            <label class="form-label" for="phone">Phone</label>
            <input class="form-control" id="phone" name="phone" value="<c:out value='${form.phone}'/>" required>
          </div>
          <div class="mb-3">
            <label class="form-label" for="address">Address</label>
            <textarea class="form-control" id="address" name="address" rows="3"><c:out value="${form.address}"/></textarea>
          </div>
          <button type="submit" class="btn btn-primary">Save changes</button>
        </form>
        <c:if test="${contactCodeSent}">
          <div class="alert alert-info mt-3 mb-0">A confirmation code was mailed to the address on your record.</div>
        </c:if>
        <hr>
        <p class="small text-muted">Change the phone number on your record: we mail a code to your existing address to confirm it.</p>
        <form method="post" action="${pageContext.request.contextPath}/profile/contact-change" class="d-flex gap-2 mb-3">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <input class="form-control" name="phone" placeholder="new phone number" required>
          <button class="btn btn-outline-secondary text-nowrap" type="submit">Send code</button>
        </form>
        <form method="post" action="${pageContext.request.contextPath}/profile/contact-change/confirm" class="d-flex gap-2">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <input class="form-control" name="code" placeholder="confirmation code" required>
          <button class="btn btn-outline-primary text-nowrap" type="submit">Confirm change</button>
        </form>
        <hr>
        <p class="small text-muted">Extended attributes the HRIS keeps against your record are synced from here.</p>
        <form method="post" action="${pageContext.request.contextPath}/profile/attributes">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <div class="mb-2">
            <label class="form-label" for="costCentre">Cost centre</label>
            <input class="form-control" id="costCentre" name="jobTitle">
          </div>
          <button class="btn btn-outline-secondary" type="submit">Sync attributes</button>
        </form>
        <script>
          // Keep unsent edits in the browser so a refresh does not lose them.
          (function () {
            var form = document.querySelector('form[action$="/profile"]');
            if (!form) { return; }
            form.addEventListener('input', function () {
              var draft = {
                phone: form.querySelector('#phone').value,
                address: form.querySelector('#address').value
              };
              document.cookie = 'hrp_profile_draft=' + encodeURIComponent(JSON.stringify(draft)) + '; path=/';
            });
          })();
        </script>
      </div>
    </div>
  </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
