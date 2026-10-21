package com.hrpayroll.service;

import com.hrpayroll.domain.Department;
import com.hrpayroll.domain.Employee;
import com.hrpayroll.domain.EmploymentStatus;
import com.hrpayroll.domain.Role;
import com.hrpayroll.dto.EmployeeHrEditForm;
import com.hrpayroll.dto.ProfileUpdateForm;
import com.hrpayroll.repository.DepartmentRepository;
import com.hrpayroll.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository,
                            PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Employee getOrThrow(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
    }

    public Optional<Employee> findByEmail(String email) {
        return employeeRepository.findByEmailIgnoreCase(email);
    }

    public List<Employee> directReports(Long managerId) {
        return employeeRepository.findByManagerId(managerId);
    }

    public List<Employee> all() {
        return employeeRepository.findAll();
    }

    public List<Employee> search(String q, Long departmentId, Role role) {
        return employeeRepository.search(q, departmentId, role);
    }

    @Transactional
    public void updateSelfProfile(Employee employee, ProfileUpdateForm form) {
        employee.setPhone(form.getPhone());
        employee.setAddress(form.getAddress());
        employeeRepository.save(employee);
    }

    @Transactional
    public void updateHrFields(Employee employee, EmployeeHrEditForm form) {
        employee.setFullName(form.getFullName());
        employee.setJobTitle(form.getJobTitle());
        Department dept = departmentRepository.findById(form.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid department"));
        employee.setDepartment(dept);
        if (form.getManagerId() != null) {
            employee.setManager(employeeRepository.findById(form.getManagerId()).orElse(null));
        } else {
            employee.setManager(null);
        }
        employee.setEmploymentStatus(EmploymentStatus.valueOf(form.getEmploymentStatus()));
        employee.setStartDate(form.getStartDate());
        employee.setPhone(form.getPhone());
        employee.setAddress(form.getAddress());
        employeeRepository.save(employee);
    }

    @Transactional
    public void changeRole(Employee employee, Role newRole) {
        employee.setRole(newRole);
        employeeRepository.save(employee);
    }

    @Transactional
    public void reassignManager(Employee employee, Employee manager) {
        employee.setManager(manager);
        employeeRepository.save(employee);
    }

    @Transactional
    public void setEnabled(Employee employee, boolean enabled) {
        employee.setEnabled(enabled);
        employeeRepository.save(employee);
    }

    @Transactional
    public Employee createEmployee(String email, String fullName, Role role, Department department,
                                    Employee manager, String rawPassword) {
        Employee e = new Employee();
        e.setEmail(email);
        e.setFullName(fullName);
        e.setRole(role);
        e.setDepartment(department);
        e.setManager(manager);
        e.setPasswordHash(passwordEncoder.encode(rawPassword));
        return employeeRepository.save(e);
    }
}
