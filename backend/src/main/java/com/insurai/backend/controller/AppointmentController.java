package com.insurai.backend.controller;

import com.insurai.backend.dto.AppointmentRequest;
import com.insurai.backend.dto.AppointmentResponse;
import com.insurai.backend.dto.AppointmentSlotDTO;
import com.insurai.backend.entity.AppointmentStatus;
import com.insurai.backend.service.AppointmentService;
import com.insurai.backend.service.AgentAvailabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AgentAvailabilityService availabilityService;

    public AppointmentController(AppointmentService appointmentService,
                                 AgentAvailabilityService availabilityService) {
        this.appointmentService = appointmentService;
        this.availabilityService = availabilityService;
    }

    // Schedule a new appointment
    @PostMapping("/schedule")
    public ResponseEntity<AppointmentResponse> scheduleAppointment(@RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.scheduleAppointment(request));
    }

    // Get appointments by agent
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<AppointmentResponse>> getAgentAppointments(@PathVariable Long agentId) {
        return ResponseEntity.ok(appointmentService.getAgentAppointments(agentId));
    }

    // NEW: get next available concrete slots for an agent (next 14 days)
    @GetMapping("/agent/{agentId}/slots")
    public ResponseEntity<List<AppointmentSlotDTO>> getAgentSlots(@PathVariable Long agentId,
                                                                  @RequestParam(required = false, defaultValue = "14") int daysAhead) {
        return ResponseEntity.ok(availabilityService.getUpcomingSlots(agentId, daysAhead));
    }

    // Get appointments by employee
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AppointmentResponse>> getEmployeeAppointments(@PathVariable Long employeeId) {
        return ResponseEntity.ok(appointmentService.getEmployeeAppointments(employeeId));
    }

    // Update appointment status
    @PutMapping("/{appointmentId}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable Long appointmentId,
            @RequestParam AppointmentStatus status
    ) {
        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(appointmentId, status));
    }
}
