package com.insurai.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class PolicyRequest {

    private String policyCode;
    private String policyName;
    private String description;
    private double premium;
    private double coverageAmount;
    private double claimLimit;
    private String policyType; // HEALTH, LIFE, VEHICLE, PROPERTY
    private String status; // ACTIVE, EXPIRED, PENDING, CANCELLED
    private String riskLevel; // LOW, MEDIUM, HIGH
    private String installmentType; // MONTHLY, QUARTERLY, YEARLY
    private String termsAndConditions;
    private int renewalNoticeDays;
    private String notes;
    private LocalDateTime creationDate;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;

    private List<Long> assignedEmployeeIds; // For multi-assignment
}
