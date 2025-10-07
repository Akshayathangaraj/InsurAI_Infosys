package com.insurai.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Employee for whom appointment is scheduled
    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonIgnoreProperties({"appointments", "claims"})
    private Employee employee;

    // Agent assigned to appointment
    @ManyToOne
    @JoinColumn(name = "agent_id")
    @JsonIgnoreProperties({"password","email"})
    private User agent;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status; // SCHEDULED, COMPLETED, CANCELLED

    private String notes; // Optional notes
}
