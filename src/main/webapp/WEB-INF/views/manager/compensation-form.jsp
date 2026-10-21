<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<h1 class="section-title h3 mb-1">Request compensation change</h1>
<p class="text-muted mb-4">For <c:out value="${employee.fullName}"/></p>

<div class="card" style="max-width:640px;">
  <div class="card-body">
    <form method="post" action="${pageContext.request.contextPath}/manager/compensation">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
      <input type="hidden" name="employeeId" value="${form.employeeId}"/>
      <div class="mb-3">
        <label class="form-label" for="proposedSalary">Proposed annual salary (USD)</label>
        <input type="number" step="0.01" min="0" class="form-control" id="proposedSalary" name="proposedSalary" required>
      </div>
      <div class="mb-3">
        <label class="form-label" for="proposedBonus">Proposed bonus (USD)</label>
        <input type="number" step="0.01" min="0" class="form-control" id="proposedBonus" name="proposedBonus" value="0.00">
      </div>
      <div class="mb-3">
        <label class="form-label" for="effectiveDate">Effective date</label>
        <input type="date" class="form-control" id="effectiveDate" name="effectiveDate" required>
      </div>
      <div class="mb-3">
        <label class="form-label" for="justification">Justification</label>
        <textarea class="form-control" id="justification" name="justification" rows="4" placeholder="Explain the reason for this change"></textarea>
      </div>
      <button type="submit" class="btn btn-primary">Submit for approval</button>
    </form>
  </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
