package com.insurai.backend.repository;

import com.insurai.backend.entity.AgentAvailability;
import com.insurai.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;

public interface AgentAvailabilityRepository extends JpaRepository<AgentAvailability, Long> {
    List<AgentAvailability> findByAgentAndIsBookedFalse(User agent);
    List<AgentAvailability> findByAgentAndDayOfWeekAndIsOffFalse(User agent, int dayOfWeek);
    List<AgentAvailability> findByAgentAndDayOfWeekAndStartTimeLessThanAndEndTimeGreaterThan(User agent, int dayOfWeek, LocalTime end, LocalTime start);
    List<AgentAvailability> findByAgent(User agent);
}
