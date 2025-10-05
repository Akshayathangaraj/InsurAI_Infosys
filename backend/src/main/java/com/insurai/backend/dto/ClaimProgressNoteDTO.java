package com.insurai.backend.dto;

import java.time.LocalDateTime;

public class ClaimProgressNoteDTO {
    private Long id;
    private Long claimId;
    private Long agentId;
    private String note;
     private String agentName;
    private LocalDateTime createdAt;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClaimId() { return claimId; }
    public void setClaimId(Long claimId) { this.claimId = claimId; }

    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
     public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
