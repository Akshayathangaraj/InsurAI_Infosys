package com.insurai.backend.service;

import com.insurai.backend.dto.ClaimRequest;
import com.insurai.backend.dto.ClaimResponse;
import com.insurai.backend.entity.*;
import com.insurai.backend.exception.ClaimValidationException;
import com.insurai.backend.repository.ClaimRepository;
import com.insurai.backend.repository.EmployeeRepository;
import com.insurai.backend.repository.PolicyRepository;
import com.insurai.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.insurai.backend.dto.ClaimProgressNoteDTO;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final EmployeeRepository employeeRepository;
    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final ClaimProgressNoteService noteService;

    public ClaimService(ClaimRepository claimRepository,
                        EmployeeRepository employeeRepository,
                        PolicyRepository policyRepository,
                        UserRepository userRepository,
                        ClaimProgressNoteService noteService) {
        this.claimRepository = claimRepository;
        this.employeeRepository = employeeRepository;
        this.policyRepository = policyRepository;
        this.userRepository = userRepository;
        this.noteService = noteService;
    }

    // ---------------- Submit Claim ----------------
    public ClaimResponse submitClaim(ClaimRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ClaimValidationException("Employee not found"));

        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new ClaimValidationException("Policy not found"));

        if (request.getAmount() <= 0)
            throw new ClaimValidationException("Claim amount must be greater than 0");

        Claim claim = new Claim();
        claim.setEmployee(employee);
        claim.setPolicy(policy);
        claim.setDescription(request.getDescription());
        claim.setAmount(request.getAmount());
        claim.setDocumentPath(request.getDocumentPath());
        claim.setClaimDate(LocalDateTime.now());
        claim.setStatus(ClaimStatus.PENDING);

        Claim savedClaim = claimRepository.save(claim);
        noteService.addNote(savedClaim.getId(), null, "Claim submitted by employee.");

        return mapToResponse(savedClaim);
    }

    // ---------------- Update Claim Status ----------------
    public ClaimResponse updateClaimStatus(Long claimId, String statusStr, Long adminId) {
    Claim claim = claimRepository.findById(claimId)
            .orElseThrow(() -> new ClaimValidationException("Claim not found"));

    ClaimStatus newStatus = ClaimStatus.valueOf(statusStr.toUpperCase());

    // FIRST get the admin user
    User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new ClaimValidationException("Admin user not found"));

    // THEN check the role
    if (admin.getRole() != Role.ADMIN)
        throw new ClaimValidationException("Only Admin can change claim status");

    // Admin can only approve/reject if status is AGENT_REVIEWED
    if (newStatus == ClaimStatus.APPROVED || newStatus == ClaimStatus.REJECTED || newStatus == ClaimStatus.SETTLED) {
        if (claim.getStatus() != ClaimStatus.AGENT_REVIEWED && claim.getStatus() != ClaimStatus.PENDING)
            throw new ClaimValidationException("Claim must be AGENT_REVIEWED before Admin approval");
    }

    // Rest of your code...
    claim.setStatus(newStatus);
    if (newStatus == ClaimStatus.APPROVED || newStatus == ClaimStatus.REJECTED || newStatus == ClaimStatus.SETTLED) {
        claim.setDecisionDate(LocalDateTime.now());
        claim.setProcessedBy(admin);
    }

    Claim updated = claimRepository.save(claim);
    noteService.addNote(updated.getId(), adminId, "Status changed to: " + newStatus.name());
    return mapToResponse(updated);
}


    // ---------------- Settle Claim ----------------
    public ClaimResponse settleClaim(Long claimId, Double settlementAmount, Long adminId, String resolutionNotes) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimValidationException("Claim not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ClaimValidationException("Admin not found"));

        if (admin.getRole() != Role.ADMIN)
            throw new ClaimValidationException("Only Admin can settle claims");

        double totalSettled = claimRepository.findByPolicy(claim.getPolicy()).stream()
                .filter(c -> c.getStatus() == ClaimStatus.SETTLED || c.getStatus() == ClaimStatus.APPROVED)
                .mapToDouble(Claim::getSettlementAmount)
                .sum();

        if (totalSettled + settlementAmount > claim.getPolicy().getClaimLimit()) {
            throw new ClaimValidationException("Settlement exceeds policy claim limit");
        }

        claim.setSettlementAmount(settlementAmount);
        claim.setStatus(ClaimStatus.SETTLED);
        claim.setDecisionDate(LocalDateTime.now());
        claim.setProcessedBy(admin);
        claim.setResolutionNotes(resolutionNotes);

        Claim updated = claimRepository.save(claim);
        noteService.addNote(updated.getId(), adminId,
                "Claim settled. Amount: " + settlementAmount + ". Notes: " + (resolutionNotes == null ? "" : resolutionNotes));
        return mapToResponse(updated);
    }

    // ---------------- Agent Suggestion ----------------
    public ClaimResponse submitAgentSuggestion(Long claimId, Long agentId, String suggestion, String notes) {
    Claim claim = claimRepository.findById(claimId)
            .orElseThrow(() -> new ClaimValidationException("Claim not found"));
    User agent = userRepository.findById(agentId)
            .orElseThrow(() -> new ClaimValidationException("Agent not found"));

    if (!agent.equals(claim.getAssignedAgent())) {
        throw new ClaimValidationException("This claim is not assigned to this agent");
    }

    if (claim.getStatus() != ClaimStatus.UNDER_REVIEW)
        throw new ClaimValidationException("Agent can only suggest for claims UNDER_REVIEW");

    String noteText = "Agent suggestion: " + suggestion;
    if (notes != null && !notes.isEmpty()) noteText += " | Notes: " + notes;

    // Add progress note
    noteService.addNote(claimId, agentId, noteText);

    // Update claim status to AGENT_REVIEWED
    claim.setStatus(ClaimStatus.AGENT_REVIEWED);
    claimRepository.save(claim);

    return mapToResponse(claim);
}

    // ---------------- Fetch Claims ----------------
    public List<ClaimResponse> getAllClaims() {
        return claimRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<ClaimResponse> getClaimsByEmployee(Long employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ClaimValidationException("Employee not found"));
        return claimRepository.findByEmployee(emp).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<ClaimResponse> getClaimsByAgent(Long agentId) {
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ClaimValidationException("Agent not found"));
        if (agent.getRole() != Role.AGENT)
            throw new ClaimValidationException("User is not an agent");
        return claimRepository.findByAssignedAgent(agent).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public Claim getClaimById(Long claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimValidationException("Claim not found"));
    }

    public void addFileToClaim(Long claimId, String filePath) {
        Claim claim = getClaimById(claimId);
        List<String> files = claim.getDocumentPaths() != null ? claim.getDocumentPaths() : new ArrayList<>();
        files.add(filePath);
        claim.setDocumentPaths(files);
        claimRepository.save(claim);
    }

    // ---------------- Claim Statistics ----------------
    public Map<String, Object> getEmployeeClaimStatistics(Long employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ClaimValidationException("Employee not found"));

        List<Claim> claims = claimRepository.findByEmployee(emp);
        double totalClaimed = claims.stream().mapToDouble(Claim::getAmount).sum();
        double totalSettled = claims.stream().filter(c -> c.getStatus() == ClaimStatus.SETTLED)
                .mapToDouble(Claim::getSettlementAmount).sum();
        long approved = claims.stream().filter(c -> c.getStatus() == ClaimStatus.APPROVED).count();
        long rejected = claims.stream().filter(c -> c.getStatus() == ClaimStatus.REJECTED).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalClaims", claims.size());
        stats.put("totalClaimedAmount", totalClaimed);
        stats.put("totalSettledAmount", totalSettled);
        stats.put("approvedCount", approved);
        stats.put("rejectedCount", rejected);

        return stats;
    }

    // ---------------- Assign Agent ----------------
    public Optional<ClaimResponse> assignAgent(Long claimId, Long agentId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimValidationException("Claim not found"));

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ClaimValidationException("Agent not found"));

        if (agent.getRole() != Role.AGENT) {
            throw new ClaimValidationException("User is not an agent");
        }

        claim.setAssignedAgent(agent);
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        Claim updated = claimRepository.save(claim);

        return Optional.of(mapToResponse(updated));
    }

    // ---------------- Helper: Mapping ----------------
    // ---------------- Helper: Mapping ----------------
private ClaimResponse mapToResponse(Claim claim) {
    // Fetch all progress notes (DTOs) for this claim
    List<ClaimProgressNoteDTO> allNoteDTOs = noteService.getNotesByClaimId(claim.getId());

    // Agent suggestions are notes that start with "Agent suggestion:"
    List<String> agentSuggestions = allNoteDTOs.stream()
        .map(ClaimProgressNoteDTO::getNote)
        .filter(note -> note.startsWith("Agent suggestion:"))
        .toList();

    // Build response including notes and documentPaths
    ClaimResponse resp = new ClaimResponse(
        claim.getId(),
        claim.getEmployee() != null ? claim.getEmployee().getId() : null,
        claim.getEmployee() != null && claim.getEmployee().getUser() != null
            ? claim.getEmployee().getUser().getUsername()
            : null,
        claim.getPolicy() != null ? claim.getPolicy().getId() : null,
        claim.getPolicy() != null ? claim.getPolicy().getPolicyName() : null,
        claim.getDescription(),
        claim.getAmount(),
        claim.getStatus() != null ? claim.getStatus().name() : null,
        claim.getDocumentPath(),
        claim.getClaimDate(),
        claim.getAssignedAgent() != null ? claim.getAssignedAgent().getId() : null,
        claim.getAssignedAgent() != null ? claim.getAssignedAgent().getUsername() : null,
        claim.getDecisionDate(),
        claim.getSettlementAmount(),
        claim.getResolutionNotes(),
        null, // old agentNotes (now unused, see below)
        agentSuggestions
    );

    // Attach the full notes DTO list (used by frontend for proper rendering)
    resp.setNotes(allNoteDTOs);

    // Also, add document paths if you have a setter:
    resp.setDocumentPaths(claim.getDocumentPaths());

    return resp;
}
    public ClaimResponse toResponse(Claim claim) {
        return mapToResponse(claim);
    }

    public ClaimResponse submitClaimWithMultipleFiles(ClaimRequest request, List<String> documentPaths) {
    Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new ClaimValidationException("Employee not found"));

    Policy policy = policyRepository.findById(request.getPolicyId())
            .orElseThrow(() -> new ClaimValidationException("Policy not found"));

    if (request.getAmount() <= 0)
        throw new ClaimValidationException("Claim amount must be greater than 0");

    Claim claim = new Claim();
    claim.setEmployee(employee);
    claim.setPolicy(policy);
    claim.setDescription(request.getDescription());
    claim.setAmount(request.getAmount());
    claim.setDocumentPath(request.getDocumentPath()); // first file for legacy
    claim.setClaimDate(LocalDateTime.now());
    claim.setStatus(ClaimStatus.PENDING);

    // IMPORTANT: store all file paths in documentPaths
    claim.setDocumentPaths(documentPaths != null ? documentPaths : new ArrayList<>());

    Claim savedClaim = claimRepository.save(claim);
    noteService.addNote(savedClaim.getId(), null, "Claim submitted by employee.");

    return mapToResponse(savedClaim);
}

}
