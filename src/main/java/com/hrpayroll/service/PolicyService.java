package com.hrpayroll.service;

import com.hrpayroll.domain.Employee;
import com.hrpayroll.domain.Policy;
import com.hrpayroll.domain.PolicyAcknowledgement;
import com.hrpayroll.dto.PolicyForm;
import com.hrpayroll.repository.PolicyAcknowledgementRepository;
import com.hrpayroll.repository.PolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyAcknowledgementRepository ackRepository;

    public PolicyService(PolicyRepository policyRepository, PolicyAcknowledgementRepository ackRepository) {
        this.policyRepository = policyRepository;
        this.ackRepository = ackRepository;
    }

    public List<Policy> activePolicies() {
        return policyRepository.findByActiveTrueOrderByTitleAsc();
    }

    public List<Policy> all() {
        return policyRepository.findAllByOrderByTitleAsc();
    }

    public Policy getOrThrow(Long id) {
        return policyRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Policy not found"));
    }

    public List<PolicyAcknowledgement> acknowledgementsFor(Long employeeId) {
        return ackRepository.findByEmployeeId(employeeId);
    }

    public boolean hasAcknowledged(Long policyId, Long employeeId, int currentVersion) {
        Optional<PolicyAcknowledgement> ack = ackRepository.findByPolicyIdAndEmployeeId(policyId, employeeId);
        return ack.isPresent() && ack.get().getPolicyVersion() >= currentVersion;
    }

    @Transactional
    public Policy create(PolicyForm form) {
        Policy p = new Policy();
        p.setTitle(form.getTitle());
        p.setContent(form.getContent());
        p.setAudience(form.getAudience());
        p.setVersion(1);
        return policyRepository.save(p);
    }

    @Transactional
    public Policy newVersion(Policy existing, PolicyForm form) {
        existing.setContent(form.getContent());
        existing.setTitle(form.getTitle());
        existing.setVersion(existing.getVersion() + 1);
        return policyRepository.save(existing);
    }

    @Transactional
    public void acknowledge(Policy policy, Employee employee) {
        Optional<PolicyAcknowledgement> existing = ackRepository.findByPolicyIdAndEmployeeId(policy.getId(), employee.getId());
        PolicyAcknowledgement ack = existing.orElseGet(PolicyAcknowledgement::new);
        ack.setPolicy(policy);
        ack.setEmployee(employee);
        ack.setPolicyVersion(policy.getVersion());
        ack.setAcknowledgedAt(java.time.Instant.now());
        ackRepository.save(ack);
    }
}
