package com.hrpayroll.service;

import com.hrpayroll.domain.Employee;
import com.hrpayroll.domain.Role;
import com.hrpayroll.repository.EmployeeRepository;
import com.hrpayroll.security.EmployeePrincipal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthorizationServiceTest {

    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    private final AuthorizationService service = new AuthorizationService(employeeRepository);

    private Employee employee(long id, Role role) {
        Employee e = new Employee();
        e.setId(id);
        e.setRole(role);
        return e;
    }

    @Test
    void employeeCanAccessOwnRecord() {
        Employee self = employee(1L, Role.EMPLOYEE);
        service.assertOwnsEmployee(new EmployeePrincipal(self), 1L);
    }

    @Test
    void employeeCannotAccessAnotherEmployeesRecord() {
        Employee self = employee(1L, Role.EMPLOYEE);
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.assertOwnsEmployee(new EmployeePrincipal(self), 2L));
    }

    @Test
    void hrAdminBypassesOwnershipCheck() {
        Employee admin = employee(1L, Role.HR_ADMIN);
        service.assertOwnsEmployee(new EmployeePrincipal(admin), 999L);
    }

    @Test
    void managerCanAccessDirectReport() {
        Employee manager = employee(1L, Role.MANAGER);
        Employee report = employee(2L, Role.EMPLOYEE);
        report.setManager(manager);
        when(employeeRepository.findById(2L)).thenReturn(java.util.Optional.of(report));

        service.assertManagesEmployee(new EmployeePrincipal(manager), 2L);
    }

    @Test
    void managerCannotAccessNonReport() {
        Employee manager = employee(1L, Role.MANAGER);
        Employee stranger = employee(3L, Role.EMPLOYEE);
        when(employeeRepository.findById(3L)).thenReturn(java.util.Optional.of(stranger));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.assertManagesEmployee(new EmployeePrincipal(manager), 3L));
    }
}
