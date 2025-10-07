package com.insurai.backend.controller;

import com.insurai.backend.dto.AgentAvailabilityDTO;
import com.insurai.backend.service.AgentAvailabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent-availability")
public class AgentAvailabilityController {

    private final AgentAvailabilityService availabilityService;

    public AgentAvailabilityController(AgentAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    // ✅ Create or update a slot
    @PostMapping("/save")
    public AgentAvailabilityDTO saveSlot(@RequestBody AgentAvailabilityDTO slotDTO) {
        return availabilityService.saveSlot(slotDTO);
    }

    // ✅ NEW: Toggle On/Off status for an existing slot
    @PostMapping("/toggle-off/{id}")
    public ResponseEntity<AgentAvailabilityDTO> toggleOff(@PathVariable Long id) {
        try {
            AgentAvailabilityDTO updated = availabilityService.toggleOff(id);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ Get all slots for an agent
    @GetMapping("/agent/{agentId}")
    public List<AgentAvailabilityDTO> getAgentSlots(@PathVariable Long agentId) {
        return availabilityService.getAgentSlots(agentId);
    }

    // ✅ Delete a slot
    @DeleteMapping("/{slotId}")
    public void deleteSlot(@PathVariable Long slotId) {
        availabilityService.deleteSlot(slotId);
    }
}
