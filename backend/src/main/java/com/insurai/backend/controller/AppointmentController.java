package com.insurai.backend.controller;

import com.insurai.backend.dto.AppointmentDTO;
import com.insurai.backend.service.AppointmentService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // ✅ Book appointment (auto-confirmed)
    @PostMapping("/book")
    public AppointmentDTO bookAppointment(@RequestParam Long employeeId,
                                          @RequestParam Long agentId,
                                          @RequestParam Long policyId,
                                          @RequestParam String startTime) {
        return appointmentService.bookAppointment(employeeId, agentId, policyId, LocalDateTime.parse(startTime));
    }

    // ✅ Get appointments by agent
    @GetMapping("/agent/{agentId}")
    public List<AppointmentDTO> getByAgent(@PathVariable Long agentId) {
        return appointmentService.getAppointmentsByAgent(agentId);
    }

    // ✅ Get appointments by employee
    @GetMapping("/employee/{employeeId}")
    public List<AppointmentDTO> getByEmployee(@PathVariable Long employeeId) {
        return appointmentService.getAppointmentsByEmployee(employeeId);
    }
}
