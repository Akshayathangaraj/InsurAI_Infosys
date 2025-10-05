package com.insurai.backend.entity;

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

    // Employee reference
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Agent reference
    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;

    // Optional: link to policy
    @ManyToOne
    @JoinColumn(name = "policy_id")
    private Policy policy;

    // Appointment time
    private LocalDateTime appointmentTime;

    // Status: PENDING, CONFIRMED, CANCELLED
    private String status = "CONFIRMED";

    // Optional: progress notes or other metadata can be linked via separate entity
}
