package com.insurai.backend.service;

import com.insurai.backend.dto.PolicyRequest;
import com.insurai.backend.entity.Employee;
import com.insurai.backend.entity.Policy;
import com.insurai.backend.entity.PolicyStatus;
import com.insurai.backend.entity.PolicyType;
import com.insurai.backend.entity.RiskLevel;
import com.insurai.backend.repository.EmployeeRepository;
import com.insurai.backend.repository.PolicyRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final EmployeeRepository employeeRepository;

    public PolicyService(PolicyRepository policyRepository, EmployeeRepository employeeRepository) {
        this.policyRepository = policyRepository;
        this.employeeRepository = employeeRepository;
    }

    // ✅ Create Policy using DTO
    public Policy createPolicy(PolicyRequest request) {
        Policy policy = new Policy();

        policy.setPolicyCode(request.getPolicyCode());
        policy.setPolicyName(request.getPolicyName());
        policy.setDescription(request.getDescription());
        policy.setPremium(request.getPremium());
        policy.setCoverageAmount(request.getCoverageAmount());
        policy.setClaimLimit(request.getClaimLimit());
        policy.setPolicyType(PolicyType.valueOf(request.getPolicyType()));
        policy.setStatus(PolicyStatus.valueOf(request.getStatus()));
        policy.setRiskLevel(RiskLevel.valueOf(request.getRiskLevel()));
        policy.setInstallmentType(request.getInstallmentType());
        policy.setTermsAndConditions(request.getTermsAndConditions());
        policy.setRenewalNoticeDays(request.getRenewalNoticeDays());
        policy.setNotes(request.getNotes());
        policy.setCreationDate(request.getCreationDate());
        policy.setEffectiveDate(request.getEffectiveDate());
        policy.setExpiryDate(request.getExpiryDate());

        if (request.getAssignedEmployeeIds() != null && !request.getAssignedEmployeeIds().isEmpty()) {
            Set<Employee> assigned = new HashSet<>();
            for (Long empId : request.getAssignedEmployeeIds()) {
                Employee emp = employeeRepository.findById(empId)
                        .orElseThrow(() -> new RuntimeException("Employee not found"));
                assigned.add(emp);
            }
            policy.setAssignedEmployees(assigned);
        }

        return policyRepository.save(policy);
    }

    // ✅ Get all policies
    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    // ✅ Get policies by employee
    public List<Policy> getPoliciesByEmployeeId(Long employeeId) {
        return policyRepository.findByAssignedEmployees_Id(employeeId);
    }

    // ✅ Assign policy to an employee (multi-assignment)
    public Optional<Policy> assignPolicy(Long policyId, Long employeeId) {
        Optional<Policy> policyOpt = policyRepository.findById(policyId);
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);

        if (policyOpt.isPresent() && employeeOpt.isPresent()) {
            Policy policy = policyOpt.get();
            policy.getAssignedEmployees().add(employeeOpt.get());
            policyRepository.save(policy);
            return Optional.of(policy);
        }
        return Optional.empty();
    }

    // ✅ Update Policy details
    public Policy updatePolicy(Long id, PolicyRequest request) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        policy.setPolicyCode(request.getPolicyCode());
        policy.setPolicyName(request.getPolicyName());
        policy.setDescription(request.getDescription());
        policy.setPremium(request.getPremium());
        policy.setCoverageAmount(request.getCoverageAmount());
        policy.setClaimLimit(request.getClaimLimit());
        policy.setPolicyType(PolicyType.valueOf(request.getPolicyType()));
        policy.setStatus(PolicyStatus.valueOf(request.getStatus()));
        policy.setRiskLevel(RiskLevel.valueOf(request.getRiskLevel()));
        policy.setInstallmentType(request.getInstallmentType());
        policy.setTermsAndConditions(request.getTermsAndConditions());
        policy.setRenewalNoticeDays(request.getRenewalNoticeDays());
        policy.setNotes(request.getNotes());
        policy.setCreationDate(request.getCreationDate());
        policy.setEffectiveDate(request.getEffectiveDate());
        policy.setExpiryDate(request.getExpiryDate());

        if (request.getAssignedEmployeeIds() != null) {
            Set<Employee> assigned = new HashSet<>();
            for (Long empId : request.getAssignedEmployeeIds()) {
                Employee emp = employeeRepository.findById(empId)
                        .orElseThrow(() -> new RuntimeException("Employee not found"));
                assigned.add(emp);
            }
            policy.setAssignedEmployees(assigned);
        }

        return policyRepository.save(policy);
    }

    // ✅ Delete Policy
    public void deletePolicy(Long id) {
        if (!policyRepository.existsById(id)) {
            throw new RuntimeException("Policy not found");
        }
        policyRepository.deleteById(id);
    }
}
