package com.insurai.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentRequest {
    private Long employeeId;
    private Long agentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String notes;
}
