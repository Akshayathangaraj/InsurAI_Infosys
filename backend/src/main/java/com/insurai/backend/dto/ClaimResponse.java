package com.insurai.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ClaimResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long policyId;
    private String policyName;
    private String description;
    private Double amount;
    private String status;
    private String documentPath;
    private LocalDateTime claimDate;
    private Long assignedAgentId;
    private String assignedAgentName;
    private LocalDateTime decisionDate;
    private Double settlementAmount;
    private String resolutionNotes;
    private String agentSuggestion;
    private String agentNotes;

    private List<ClaimProgressNoteDTO> notes;

    public ClaimResponse() {}

    public ClaimResponse(Long id,
                         Long employeeId,
                         String employeeName,
                         Long policyId,
                         String policyName,
                         String description,
                         Double amount,
                         String status,
                         String documentPath,
                         LocalDateTime claimDate,
                         Long assignedAgentId,
                         String assignedAgentName,
                         LocalDateTime decisionDate,
                         Double settlementAmount,
                         String resolutionNotes,
                         String agentSuggestion,
                         String agentNotes) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.policyId = policyId;
        this.policyName = policyName;
        this.description = description;
        this.amount = amount;
        this.status = status;
        this.documentPath = documentPath;
        this.claimDate = claimDate;
        this.assignedAgentId = assignedAgentId;
        this.assignedAgentName = assignedAgentName;
        this.decisionDate = decisionDate;
        this.settlementAmount = settlementAmount;
        this.resolutionNotes = resolutionNotes;
        this.agentSuggestion = agentSuggestion;
        this.agentNotes = agentNotes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }

    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }

    public LocalDateTime getClaimDate() { return claimDate; }
    public void setClaimDate(LocalDateTime claimDate) { this.claimDate = claimDate; }

    public Long getAssignedAgentId() { return assignedAgentId; }
    public void setAssignedAgentId(Long assignedAgentId) { this.assignedAgentId = assignedAgentId; }

    public String getAssignedAgentName() { return assignedAgentName; }
    public void setAssignedAgentName(String assignedAgentName) { this.assignedAgentName = assignedAgentName; }

    public LocalDateTime getDecisionDate() { return decisionDate; }
    public void setDecisionDate(LocalDateTime decisionDate) { this.decisionDate = decisionDate; }

    public Double getSettlementAmount() { return settlementAmount; }
    public void setSettlementAmount(Double settlementAmount) { this.settlementAmount = settlementAmount; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    // public String getAgentSuggestion() { return agentSuggestion; }
    // public void setAgentSuggestion(String agentSuggestion) { this.agentSuggestion = agentSuggestion; }

    // public String getAgentNotes() { return agentNotes; }
    // public void setAgentNotes(String agentNotes) { this.agentNotes = agentNotes; }

    public List<ClaimProgressNoteDTO> getNotes() { return notes; }
    public void setNotes(List<ClaimProgressNoteDTO> notes) { this.notes = notes; }
}
