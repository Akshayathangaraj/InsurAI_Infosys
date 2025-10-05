package com.insurai.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "claims")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;      // claim description
    private Double amount;           // claim amount

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;  
        // PENDING, UNDER_REVIEW, APPROVED, REJECTED, SETTLED

    // Single file path (for backward compatibility)
    @Column(name = "document_path")
    private String documentPath;

    // Multiple files support
    @ElementCollection
    @CollectionTable(name = "claim_documents", joinColumns = @JoinColumn(name = "claim_id"))
    @Column(name = "document_path")
    private List<String> documentPaths = new ArrayList<>();

    private LocalDateTime claimDate = LocalDateTime.now(); // timestamp when submitted
    private LocalDateTime decisionDate; // when approved/rejected/settled
    private Double settlementAmount; // amount settled/paid if any

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes; // notes added at approval/rejection

    // FK pointing to Employee.id
    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    @JsonBackReference
    private Employee employee;

    // FK pointing to Policy.id (optional)
    @ManyToOne
    @JoinColumn(name = "policy_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"policies", "claims", "assignedEmployee"})
    private Policy policy;

    // Who (agent) is assigned (optional)
    @ManyToOne
    @JoinColumn(name = "assigned_agent_id")
    @JsonIgnoreProperties({"password","email"})
    private User assignedAgent;

    // who processed final decision (admin)
    @ManyToOne
    @JoinColumn(name = "processed_by_id")
    @JsonIgnoreProperties({"password","email"})
    private User processedBy;
}
