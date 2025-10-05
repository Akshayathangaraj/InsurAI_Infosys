package com.insurai.backend.dto;

public class AddClaimNoteRequest {
    private Long claimId;
    private Long agentId;
    private String note;

    public Long getClaimId() { return claimId; }
    public void setClaimId(Long claimId) { this.claimId = claimId; }

    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
