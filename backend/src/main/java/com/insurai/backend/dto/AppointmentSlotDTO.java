package com.insurai.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentSlotDTO {
    private Long availabilityId;   // Refers to the AgentAvailability entry
    private Long agentId;           // For identifying which agent
    private String agentName;       // To display agent name
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean booked;         // True if already booked
    private boolean off;            // True if agent marked this time off
}
