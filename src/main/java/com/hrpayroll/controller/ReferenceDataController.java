package com.hrpayroll.controller;

import com.hrpayroll.domain.Department;
import com.hrpayroll.domain.Policy;
import com.hrpayroll.service.DepartmentService;
import com.hrpayroll.service.EmployeeDirectoryService;
import com.hrpayroll.service.PayrollFeedService;
import com.hrpayroll.service.PolicyService;
import com.hrpayroll.util.ETagUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Read-only reference-data JSON endpoints consumed by the UI for
 * dropdowns/filters. Departments and policies are non-sensitive, low-churn
 * lists, so the response carries a weak ETag (see ETagUtil - MD5, benign,
 * precision baseline primitive #3) to let the browser cache them.
 */
@RestController
@RequestMapping("/api/reference")
public class ReferenceDataController {

    private final DepartmentService departmentService;
    private final PolicyService policyService;
    private final EmployeeDirectoryService employeeDirectoryService;
    private final PayrollFeedService payrollFeedService;

    public ReferenceDataController(DepartmentService departmentService, PolicyService policyService,
                                    EmployeeDirectoryService employeeDirectoryService,
                                    PayrollFeedService payrollFeedService) {
        this.departmentService = departmentService;
        this.policyService = policyService;
        this.employeeDirectoryService = employeeDirectoryService;
        this.payrollFeedService = payrollFeedService;
    }

    /**
     * Connection state of the outbound HRIS feed, read by the import/export
     * screen's status strip so HR can see which profile is currently loaded.
     */
    @GetMapping("/feed-profile")
    public Map<String, Object> feedCfg() {
        return Map.of(
                "configured", payrollFeedService.isConfigured(),
                "endpoint", payrollFeedService.getEndpointUrl(),
                "partnerAccount", payrollFeedService.getPartnerAcct(),
                "signingKey", payrollFeedService.getSigningKey());
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Map<String, Object>>> departments() {
        List<Department> departments = departmentService.all();
        String body = departments.stream()
                .map(d -> d.getId() + ":" + d.getName())
                .collect(Collectors.joining(","));
        String etag = ETagUtil.weakETag(body);
        List<Map<String, Object>> payload = departments.stream()
                .map(d -> Map.<String, Object>of("id", d.getId(), "name", d.getName()))
                .toList();
        return ResponseEntity.ok().eTag(etag).body(payload);
    }

    @GetMapping("/policies")
    public ResponseEntity<List<Map<String, Object>>> policies() {
        List<Policy> policies = policyService.activePolicies();
        String body = policies.stream()
                .map(p -> p.getId() + ":" + p.getVersion())
                .collect(Collectors.joining(","));
        String etag = ETagUtil.weakETag(body);
        List<Map<String, Object>> payload = policies.stream()
                .map(p -> Map.<String, Object>of("id", p.getId(), "title", p.getTitle(), "version", p.getVersion()))
                .toList();
        return ResponseEntity.ok().eTag(etag).body(payload);
    }

    /** Bounded, whitelisted-column sort (precision baseline primitive #4). */
    @GetMapping("/employees")
    public List<Map<String, Object>> employeeDirectory(@RequestParam(defaultValue = "full_name") String sort,
                                                         @RequestParam(defaultValue = "asc") String dir,
                                                         @RequestParam(required = false) Long departmentId) {
        return employeeDirectoryService.listSorted(sort, dir, departmentId);
    }
}
