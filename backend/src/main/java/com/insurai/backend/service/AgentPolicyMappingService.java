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

    public AgentPolicyMappingService(AgentPolicyMappingRepository mappingRepo,
                                     UserRepository userRepo,
                                     PolicyRepository policyRepo) {
        this.mappingRepo = mappingRepo;
        this.userRepo = userRepo;
        this.policyRepo = policyRepo;
    }

    // ✅ Assign policy to an agent
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

    // ✅ Get all policies assigned to a specific agent
    public List<AgentPolicyMapping> getPoliciesByAgent(Long agentId) {
        User agent = userRepo.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        return mappingRepo.findByAgent(agent);
    }

    // ✅ Build summarized context for chatbot or analytics
    public String buildContextFromDatabase() {
        StringBuilder context = new StringBuilder();
        context.append("\n\n📜 Agent–Policy Mapping Summary\n");
        context.append("=================================\n");

        try {
            List<AgentPolicyMapping> mappings = mappingRepo.findAll();

            if (mappings.isEmpty()) {
                context.append("No agent–policy mappings found in the system.\n");
                return context.toString();
            }

            for (AgentPolicyMapping mapping : mappings) {
                User agent = mapping.getAgent();
                Policy policy = mapping.getPolicy();

                // ✅ Safe null handling
                String agentName = (agent != null && agent.getUsername() != null)
                        ? agent.getUsername() : "Unknown Agent";
                String agentEmail = (agent != null && agent.getEmail() != null)
                        ? agent.getEmail() : "No Email";

                String policyName = (policy != null && policy.getPolicyName() != null)
                        ? policy.getPolicyName() : "Unnamed Policy";
                String policyType = (policy != null && policy.getPolicyType() != null)
                        ? policy.getPolicyType().name() : "N/A";
                String coverage = (policy != null)
                        ? String.valueOf(policy.getCoverageAmount()) : "N/A";
                String premium = (policy != null)
                        ? String.valueOf(policy.getPremium()) : "N/A";
                String status = (policy != null && policy.getStatus() != null)
                        ? policy.getStatus().name() : "UNKNOWN";

                context.append(String.format(
                        "👤 Agent: %s (%s)\n" +
                        "   • Policy: %s\n" +
                        "   • Type: %s\n" +
                        "   • Coverage: ₹%s\n" +
                        "   • Premium: ₹%s\n" +
                        "   • Status: %s\n" +
                        "---------------------------------\n",
                        agentName,
                        agentEmail,
                        policyName,
                        policyType,
                        coverage,
                        premium,
                        status
                ));
            }

        } catch (Exception e) {
            context.append("\n⚠️ Error building agent-policy context: ").append(e.getMessage()).append("\n");
        }

        return context.toString();
    }
}
