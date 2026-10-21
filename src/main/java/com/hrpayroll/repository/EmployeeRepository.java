package com.hrpayroll.repository;

import com.hrpayroll.domain.Employee;
import com.hrpayroll.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmailIgnoreCase(String email);

    List<Employee> findByManagerId(Long managerId);

    List<Employee> findByDepartmentId(Long departmentId);

    List<Employee> findByRole(Role role);

    @Query("select e from Employee e where " +
            "(:pattern is null or lower(e.fullName) like :pattern or lower(e.email) like :pattern) " +
            "and (:departmentId is null or e.department.id = :departmentId) " +
            "and (:role is null or e.role = :role)")
    List<Employee> searchByPattern(@Param("pattern") String likePattern, @Param("departmentId") Long departmentId, @Param("role") Role role);

    default List<Employee> search(String q, Long departmentId, Role role) {
        String pattern = (q == null || q.isBlank()) ? null : "%" + q.toLowerCase() + "%";
        return searchByPattern(pattern, departmentId, role);
    }
}
