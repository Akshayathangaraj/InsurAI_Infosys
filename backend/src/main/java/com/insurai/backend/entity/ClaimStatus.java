package com.insurai.backend.entity;

public enum ClaimStatus {
    PENDING,        // submitted by employee
    UNDER_REVIEW,   // assigned to an agent or being reviewed
    APPROVED,
    REJECTED,
    SETTLED         // payment completed
}
