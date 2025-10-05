package com.insurai.backend.service;

import com.insurai.backend.entity.AgentPolicyMapping;
import com.insurai.backend.entity.Policy;
import com.insurai.backend.entity.User;
import com.insurai.backend.repository.AgentPolicyMappingRepository;
import com.insurai.backend.repository.PolicyRepository;
import com.insurai.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentPolicyMappingService {

    private final AgentPolicyMappingRepository mappingRepo;
    private final UserRepository userRepo;
    private final PolicyRepository policyRepo;

    public AgentPolicyMappingService(AgentPolicyMappingRepository mappingRepo, UserRepository userRepo, PolicyRepository policyRepo) {
        this.mappingRepo = mappingRepo;
        this.userRepo = userRepo;
        this.policyRepo = policyRepo;
    }

    // ✅ Assign policy to agent
    public AgentPolicyMapping assignPolicyToAgent(Long agentId, Long policyId) {
        User agent = userRepo.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        Policy policy = policyRepo.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        boolean exists = mappingRepo.existsByAgentAndPolicy(agent, policy);
        if (exists) {
            throw new RuntimeException("Policy already assigned to this agent");
        }

        AgentPolicyMapping mapping = new AgentPolicyMapping();
        mapping.setAgent(agent);
        mapping.setPolicy(policy);

        return mappingRepo.save(mapping);
    }

    // ✅ Get all policies assigned to agent
    public List<AgentPolicyMapping> getPoliciesByAgent(Long agentId) {
        User agent = userRepo.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        return mappingRepo.findByAgent(agent);
    }
}
