package com.insurai.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String policyCode; // Unique code, e.g., HCP-2025-001

    @Column(nullable = false)
    private String policyName;

    private String description;

    private double premium;

    private double coverageAmount;

    private double claimLimit;

    @Enumerated(EnumType.STRING)
    private PolicyType policyType; // HEALTH, LIFE, VEHICLE, PROPERTY

    @Enumerated(EnumType.STRING)
    private PolicyStatus status; // ACTIVE, EXPIRED, PENDING, CANCELLED

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel; // LOW, MEDIUM, HIGH

    private String installmentType; // MONTHLY, QUARTERLY, YEARLY

    @Column(columnDefinition = "TEXT")
    private String termsAndConditions;

    private int renewalNoticeDays;

    private String notes;

    private LocalDateTime creationDate;

    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    // Many-to-Many: A policy can be assigned to multiple employees
    @ManyToMany
    @JoinTable(
        name = "policy_assignments",
        joinColumns = @JoinColumn(name = "policy_id"),
        inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    @JsonIgnoreProperties({"policies", "claims", "appointments"})
    private Set<Employee> assignedEmployees = new HashSet<>();
}
