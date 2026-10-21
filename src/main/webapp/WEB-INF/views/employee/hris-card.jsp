<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-4">HRIS data card</h1>

<div class="row g-4">
  <div class="col-lg-5">
    <div class="card">
      <div class="card-header fw-semibold">Staff member</div>
      <div class="card-body">
        <dl class="row mb-0">
          <dt class="col-sm-5">Full name</dt><dd class="col-sm-7"><c:out value="${identified.fullName}"/></dd>
          <dt class="col-sm-5">Email</dt><dd class="col-sm-7"><c:out value="${identified.email}"/></dd>
          <dt class="col-sm-5">Job title</dt><dd class="col-sm-7"><c:out value="${identified.jobTitle}"/></dd>
          <dt class="col-sm-5">Department</dt><dd class="col-sm-7"><c:out value="${identified.department.name}"/></dd>
        </dl>
      </div>
    </div>
  </div>
  <div class="col-lg-7">
    <div class="card">
      <div class="card-header fw-semibold">Pay summary</div>
      <div class="card-body p-0">
        <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead><tr><th>Effective date</th><th>Salary</th><th>Bonus</th></tr></thead>
          <tbody>
          <c:forEach var="rec" items="${history}">
            <tr>
              <td><c:out value="${rec.effectiveDate}"/></td>
              <td><c:out value="${rec.salary}"/></td>
              <td><c:out value="${rec.bonus}"/></td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
        </div>
      </div>
    </div>
  </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
