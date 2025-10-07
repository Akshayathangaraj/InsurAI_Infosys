package com.insurai.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "agent_availability")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;

    private int dayOfWeek; // 1=Monday, 7=Sunday

    private LocalTime startTime;
    private LocalTime endTime;

    private boolean isBooked = false;
    private boolean isOff = false; // for off-day toggle
}
