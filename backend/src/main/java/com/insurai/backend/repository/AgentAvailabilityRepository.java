package com.insurai.backend.repository;

import com.insurai.backend.entity.AgentAvailability;
import com.insurai.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AgentAvailabilityRepository extends JpaRepository<AgentAvailability, Long> {
    List<AgentAvailability> findByAgentAndIsBookedFalse(User agent);
    List<AgentAvailability> findByAgentAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(User agent, LocalDateTime end, LocalDateTime start);
}
