package com.insurai.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim_progress_notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimProgressNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "claim_id")
    private Claim claim;

    @ManyToOne
    @JoinColumn(name = "agent_id")
    private User agent;

    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();
}
