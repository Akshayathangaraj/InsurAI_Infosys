package com.insurai.backend.controller;

import com.insurai.backend.dto.PolicyRequest;
import com.insurai.backend.entity.Policy;
import com.insurai.backend.service.PolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    // ✅ Create Policy (Admin Only)
    @PostMapping("/create")
    public ResponseEntity<Policy> createPolicy(@RequestBody PolicyRequest request) {
        return ResponseEntity.ok(policyService.createPolicy(request));
    }

    // ✅ View All Policies
    @GetMapping
    public ResponseEntity<List<Policy>> getAllPolicies() {
        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    // ✅ Get Policies by Employee
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Policy>> getPoliciesByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(policyService.getPoliciesByEmployeeId(employeeId));
    }

    // ✅ Assign Policy to Employee
    @PutMapping("/{policyId}/assign/{employeeId}")
    public ResponseEntity<Policy> assignPolicy(@PathVariable Long policyId, @PathVariable Long employeeId) {
        return policyService.assignPolicy(policyId, employeeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    // ✅ Update Policy
@PutMapping("/{id}")
public ResponseEntity<Policy> updatePolicy(@PathVariable Long id, @RequestBody PolicyRequest request) {
    return ResponseEntity.ok(policyService.updatePolicy(id, request));
}

// ✅ Delete Policy
@DeleteMapping("/{id}")
public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
    policyService.deletePolicy(id);
    return ResponseEntity.noContent().build();
}

}
