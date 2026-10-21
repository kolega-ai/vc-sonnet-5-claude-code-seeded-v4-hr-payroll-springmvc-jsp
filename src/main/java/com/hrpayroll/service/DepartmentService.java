package com.hrpayroll.service;

import com.hrpayroll.domain.Department;
import com.hrpayroll.dto.DepartmentForm;
import com.hrpayroll.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<Department> all() {
        return departmentRepository.findAll();
    }

    public Department getOrThrow(Long id) {
        return departmentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Department not found"));
    }

    @Transactional
    public Department create(DepartmentForm form) {
        Department d = new Department();
        d.setName(form.getName());
        d.setDescription(form.getDescription());
        return departmentRepository.save(d);
    }
}
