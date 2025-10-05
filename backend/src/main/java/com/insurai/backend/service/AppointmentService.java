package com.insurai.backend.service;

import com.insurai.backend.dto.AppointmentDTO;
import com.insurai.backend.entity.*;
import com.insurai.backend.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final UserRepository userRepo;
    private final EmployeeRepository employeeRepo;
    private final PolicyRepository policyRepo;
    private final AgentAvailabilityRepository availabilityRepo;
    private final AgentPolicyMappingRepository mappingRepo;

    public AppointmentService(AppointmentRepository appointmentRepo,
                              UserRepository userRepo,
                              EmployeeRepository employeeRepo,
                              PolicyRepository policyRepo,
                              AgentAvailabilityRepository availabilityRepo,
                              AgentPolicyMappingRepository mappingRepo) {
        this.appointmentRepo = appointmentRepo;
        this.userRepo = userRepo;
        this.employeeRepo = employeeRepo;
        this.policyRepo = policyRepo;
        this.availabilityRepo = availabilityRepo;
        this.mappingRepo = mappingRepo;
    }

    public AppointmentDTO bookAppointment(Long employeeId, Long agentId, Long policyId, LocalDateTime startTime) {
        // Validate inputs
        if (employeeId == null || agentId == null || policyId == null || startTime == null) {
            throw new RuntimeException("All input fields are required");
        }

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Appointment time cannot be in the past");
        }

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        User agent = userRepo.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        Policy policy = policyRepo.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        // Check if agent has the policy assigned
        boolean hasPolicy = mappingRepo.existsByAgentAndPolicy(agent, policy);
        if (!hasPolicy) {
            throw new RuntimeException("Agent is not authorized for this policy");
        }

        // Check slot availability
        AgentAvailability slot = availabilityRepo.findByAgentAndIsBookedFalse(agent)
                .stream()
                .filter(s -> !startTime.isBefore(s.getStartTime()) && !startTime.isAfter(s.getEndTime()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No available slot at the requested time"));

        // Prevent employee double-booking
        boolean conflict = appointmentRepo.existsByEmployeeAndAppointmentTime(employee, startTime);
        if (conflict) {
            throw new RuntimeException("Employee already has an appointment at this time");
        }

        // Book slot
        slot.setBooked(true);
        availabilityRepo.save(slot);

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setEmployee(employee);
        appointment.setAgent(agent);
        appointment.setPolicy(policy);
        appointment.setAppointmentTime(startTime);
        appointment.setStatus("CONFIRMED");

        Appointment saved = appointmentRepo.save(appointment);
        return mapToDTO(saved);
    }

    public List<AppointmentDTO> getAppointmentsByAgent(Long agentId) {
        if (agentId == null) throw new RuntimeException("Agent ID cannot be null");

        User agent = userRepo.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        return appointmentRepo.findByAgent(agent)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAppointmentsByEmployee(Long employeeId) {
        if (employeeId == null) throw new RuntimeException("Employee ID cannot be null");

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return appointmentRepo.findByEmployee(employee)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AppointmentDTO mapToDTO(Appointment app) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(app.getId());
        dto.setEmployeeId(app.getEmployee().getId());
        dto.setAgentId(app.getAgent().getId());
        dto.setPolicyId(app.getPolicy().getId());
        dto.setAppointmentTime(app.getAppointmentTime());
        dto.setStatus(app.getStatus());
        return dto;
    }
}
