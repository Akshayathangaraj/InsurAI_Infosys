package com.insurai.backend.controller;

import com.insurai.backend.dto.AgentAvailabilityDTO;
import com.insurai.backend.service.AgentAvailabilityService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/agent-availability")
public class AgentAvailabilityController {

    private final AgentAvailabilityService availabilityService;

    public AgentAvailabilityController(AgentAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping("/create")
    public AgentAvailabilityDTO createSlot(@RequestParam Long agentId,
                                           @RequestParam String start,
                                           @RequestParam String end) {
        return availabilityService.createSlot(agentId,
                LocalDateTime.parse(start),
                LocalDateTime.parse(end));
    }

    @GetMapping("/agent/{agentId}")
    public List<AgentAvailabilityDTO> getAgentSlots(@PathVariable Long agentId) {
        return availabilityService.getAgentSlots(agentId);
    }
}
