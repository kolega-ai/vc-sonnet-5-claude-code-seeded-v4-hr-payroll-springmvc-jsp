package com.hrpayroll.service;

import com.hrpayroll.domain.Employee;
import com.hrpayroll.domain.Role;
import com.hrpayroll.repository.EmployeeRepository;
import com.hrpayroll.security.EmployeePrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Central authorization service. Controllers call into this service - the
 * ownership/scope check itself lives here, one call frame above the
 * controller method, not inline in the controller. This is precision
 * baseline primitive #1 (Corpus Shared Contract): "An ownership/scope check
 * performed in a shared service one call frame above the controller method,
 * rather than inline."
 */
@Service
public class AuthorizationService {

    private final EmployeeRepository employeeRepository;

    public AuthorizationService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /** Employees may only act on their own record unless they hold an HR/payroll role. */
    public void assertOwnsEmployee(EmployeePrincipal actor, Long targetEmployeeId) {
        if (actor.getEmployee().getRole() == Role.HR_ADMIN) {
            return;
        }
        if (!actor.getId().equals(targetEmployeeId)) {
            throw new AccessDeniedException("Not authorized to access employee " + targetEmployeeId);
        }
    }

    /** Managers may view/act on direct reports only (or their own record); HR admin sees all. */
    public void assertManagesEmployee(EmployeePrincipal actor, Long targetEmployeeId) {
        Employee actorEmp = actor.getEmployee();
        if (actorEmp.getRole() == Role.HR_ADMIN) {
            return;
        }
        if (actorEmp.getId().equals(targetEmployeeId)) {
            return;
        }
        if (actorEmp.getRole() == Role.MANAGER) {
            Employee target = employeeRepository.findById(targetEmployeeId)
                    .orElseThrow(() -> new AccessDeniedException("No such employee"));
            if (target.getManager() != null && target.getManager().getId().equals(actorEmp.getId())) {
                return;
            }
        }
        throw new AccessDeniedException("Not authorized to manage employee " + targetEmployeeId);
    }

    public void assertRole(EmployeePrincipal actor, Role... allowed) {
        for (Role r : allowed) {
            if (actor.getEmployee().getRole() == r) {
                return;
            }
        }
        throw new AccessDeniedException("Role not permitted");
    }
}
