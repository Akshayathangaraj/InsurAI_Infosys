package com.insurai.backend.controller;

import com.insurai.backend.entity.AgentPolicyMapping;
import com.insurai.backend.entity.Policy;
import com.insurai.backend.service.AgentPolicyMappingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/agent-policies")
public class AgentPolicyController {

    private final AgentPolicyMappingService agentPolicyService;

    public AgentPolicyController(AgentPolicyMappingService agentPolicyService) {
        this.agentPolicyService = agentPolicyService;
    }

    // ✅ Assign policy to agent
    @PostMapping("/assign")
    public ResponseEntity<String> assignPolicyToAgent(@RequestParam Long agentId,
                                                      @RequestParam Long policyId) {
        agentPolicyService.assignPolicyToAgent(agentId, policyId);
        return ResponseEntity.ok("Policy assigned successfully");
    }

    // ✅ Get all policies assigned to an agent
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<Policy>> getPoliciesByAgent(@PathVariable Long agentId) {
        List<Policy> policies = agentPolicyService.getPoliciesByAgent(agentId)
                .stream()
                .map(AgentPolicyMapping::getPolicy)
                .collect(Collectors.toList());
        return ResponseEntity.ok(policies);
    }
}
