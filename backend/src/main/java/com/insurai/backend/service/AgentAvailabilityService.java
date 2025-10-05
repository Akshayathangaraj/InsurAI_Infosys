package com.insurai.backend.service;

import com.insurai.backend.dto.AgentAvailabilityDTO;
import com.insurai.backend.entity.AgentAvailability;
import com.insurai.backend.entity.User;
import com.insurai.backend.repository.AgentAvailabilityRepository;
import com.insurai.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgentAvailabilityService {

    private final AgentAvailabilityRepository availabilityRepo;
    private final UserRepository userRepo;

    public AgentAvailabilityService(AgentAvailabilityRepository availabilityRepo, UserRepository userRepo) {
        this.availabilityRepo = availabilityRepo;
        this.userRepo = userRepo;
    }

    public AgentAvailabilityDTO createSlot(Long agentId, LocalDateTime start, LocalDateTime end) {
        // Validate inputs
        if (agentId == null || start == null || end == null) {
            throw new RuntimeException("Agent ID, start time, and end time are required");
        }

        if (start.isAfter(end) || start.equals(end)) {
            throw new RuntimeException("Start time must be before end time");
        }

        if (start.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Start time cannot be in the past");
        }

        User agent = userRepo.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        // Check for overlapping slots
        List<AgentAvailability> overlapping = availabilityRepo.findByAgentAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(agent, end, start);
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Slot overlaps with existing availability");
        }

        AgentAvailability slot = new AgentAvailability();
        slot.setAgent(agent);
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setBooked(false);

        AgentAvailability saved = availabilityRepo.save(slot);
        return mapToDTO(saved);
    }

    public List<AgentAvailabilityDTO> getAgentSlots(Long agentId) {
        if (agentId == null) throw new RuntimeException("Agent ID cannot be null");

        User agent = userRepo.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        return availabilityRepo.findByAgentAndIsBookedFalse(agent)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AgentAvailabilityDTO mapToDTO(AgentAvailability slot) {
        AgentAvailabilityDTO dto = new AgentAvailabilityDTO();
        dto.setId(slot.getId());
        dto.setAgentId(slot.getAgent().getId());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setBooked(slot.isBooked());
        return dto;
    }
}
