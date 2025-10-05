package com.insurai.backend.repository;

import com.insurai.backend.entity.AgentPolicyMapping;
import com.insurai.backend.entity.User;
import com.insurai.backend.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AgentPolicyMappingRepository extends JpaRepository<AgentPolicyMapping, Long> {
    List<AgentPolicyMapping> findByAgent(User agent);
     boolean existsByAgentAndPolicy(User agent, Policy policy);
    List<AgentPolicyMapping> findByPolicy(Policy policy);
}
