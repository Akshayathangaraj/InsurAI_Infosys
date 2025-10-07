package com.insurai.backend.service;

import com.insurai.backend.dto.AppointmentRequest;
import com.insurai.backend.dto.AppointmentResponse;
import com.insurai.backend.entity.Appointment;
import com.insurai.backend.entity.AppointmentStatus;
import com.insurai.backend.entity.AgentAvailability;
import com.insurai.backend.entity.Employee;
import com.insurai.backend.entity.User;
import com.insurai.backend.repository.AppointmentRepository;
import com.insurai.backend.repository.EmployeeRepository;
import com.insurai.backend.repository.UserRepository;
import com.insurai.backend.repository.AgentAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final AgentAvailabilityRepository availabilityRepository;

    @Autowired
    private EmailService emailService; // <-- Inject EmailService

    public AppointmentService(AppointmentRepository appointmentRepository,
                              EmployeeRepository employeeRepository,
                              UserRepository userRepository,
                              AgentAvailabilityRepository availabilityRepository) {
        this.appointmentRepository = appointmentRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.availabilityRepository = availabilityRepository;
    }

    public AppointmentResponse scheduleAppointment(AppointmentRequest request) {
        // Basic presence checks
        if (request.getEmployeeId() == null || request.getAgentId() == null ||
            request.getStartTime() == null || request.getEndTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "employeeId, agentId, startTime and endTime are required");
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        User agent = userRepository.findById(request.getAgentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agent not found"));

        // Check time validity
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time must be before end time");
        }

        // Check agent weekly availability
        DayOfWeek dow = request.getStartTime().getDayOfWeek();
        int dowValue = dow.getValue();
        LocalTime reqStartLocal = request.getStartTime().toLocalTime();
        LocalTime reqEndLocal = request.getEndTime().toLocalTime();

        List<AgentAvailability> availList = availabilityRepository.findByAgent(agent).stream()
                .filter(a -> a.getDayOfWeek() == dowValue && !a.isOff())
                .filter(a -> !a.getStartTime().isAfter(reqStartLocal) && !a.getEndTime().isBefore(reqEndLocal))
                .collect(Collectors.toList());

        if (availList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested time is outside agent's availability or agent is off");
        }

        // Check overlapping appointments
        List<Appointment> overlapping = appointmentRepository.findByAgentAndStartTimeLessThanAndEndTimeGreaterThan(
                agent, request.getEndTime(), request.getStartTime()
        );

        if (!overlapping.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Agent already has an appointment in this time slot");
        }

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setEmployee(employee);
        appointment.setAgent(agent);
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setNotes(request.getNotes());

        Appointment saved = appointmentRepository.save(appointment);

        // ------------------------------
        // Send email notification to agent
        // ------------------------------
        if (agent.getEmail() != null) {
            String subject = "New Appointment Booked";
            String body = "Hello " + agent.getUsername() + ",\n\n" +
                    "A new appointment has been booked by " + employee.getUser().getUsername() + ".\n" +
                    "Date & Time: " + request.getStartTime() + " to " + request.getEndTime() + "\n\n" +
                    "Please check your dashboard for details.\n\nRegards,\nInsurance Team";
            emailService.sendEmail(agent.getEmail(), subject, body);
        }

        return toResponse(saved);
    }

    public List<AppointmentResponse> getAgentAppointments(Long agentId) {
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agent not found"));
        return appointmentRepository.findByAgent(agent)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getEmployeeAppointments(Long employeeId) {
        return appointmentRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AppointmentResponse updateAppointmentStatus(Long appointmentId, AppointmentStatus status) {
    Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    appointment.setStatus(status);
    return toResponse(appointmentRepository.save(appointment));
}

// -------------------------
// NEW: Mark appointments as MISSED after grace period
// -------------------------
public void markMissedAppointments() {
    LocalDateTime now = LocalDateTime.now();
    List<Appointment> toMarkMissed = appointmentRepository.findByStatus(AppointmentStatus.SCHEDULED)
            .stream()
            .filter(a -> a.getEndTime().plusMinutes(15).isBefore(now)) // 15 min grace period
            .collect(Collectors.toList());

    for (Appointment a : toMarkMissed) {
        a.setStatus(AppointmentStatus.MISSED);
        appointmentRepository.save(a);

        // Optional: send email to agent
        if (a.getAgent() != null && a.getAgent().getEmail() != null) {
            String subject = "Appointment Missed Notification";
            String body = "Hello " + a.getAgent().getUsername() + ",\n\n" +
                    "Appointment ID " + a.getId() + " scheduled from " + 
                    a.getStartTime() + " to " + a.getEndTime() + " was missed.\n\nRegards,\nInsurance Team";
            emailService.sendEmail(a.getAgent().getEmail(), subject, body);
        }
    }
}


    public AppointmentResponse toResponse(Appointment appointment) {
        AppointmentResponse res = new AppointmentResponse();
        res.setId(appointment.getId());

        // Employee info
        if (appointment.getEmployee() != null && appointment.getEmployee().getUser() != null) {
            res.setEmployeeId(appointment.getEmployee().getId());
            res.setEmployeeName(appointment.getEmployee().getUser().getUsername());
        } else {
            res.setEmployeeName("Unknown");
        }

        // Agent info
        if (appointment.getAgent() != null) {
            res.setAgentId(appointment.getAgent().getId());
            res.setAgentName(appointment.getAgent().getUsername());
        } else {
            res.setAgentName("Unknown");
        }

        res.setStartTime(appointment.getStartTime());
        res.setEndTime(appointment.getEndTime());
        res.setStatus(appointment.getStatus());
        res.setNotes(appointment.getNotes());

        return res;
    }
}
