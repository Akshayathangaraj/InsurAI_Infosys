package com.insurai.backend.dto;

import com.insurai.backend.entity.AppointmentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long agentId;
    private String agentName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AppointmentStatus status;
    private String notes;
}
