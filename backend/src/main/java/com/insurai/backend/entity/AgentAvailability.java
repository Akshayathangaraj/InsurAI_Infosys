package com.insurai.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean isBooked = false;
}
