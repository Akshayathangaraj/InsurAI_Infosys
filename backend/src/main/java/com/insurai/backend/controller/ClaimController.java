package com.insurai.backend.controller;

import com.insurai.backend.dto.ClaimRequest;
import com.insurai.backend.dto.ClaimResponse;
import com.insurai.backend.entity.Claim;
import com.insurai.backend.entity.User;
import com.insurai.backend.service.ClaimService;
import com.insurai.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;
    private final UserRepository userRepository;

    @Value("${file.upload-dir:${user.home}/insurai_uploads/claims}")
    private String uploadDir;

    public ClaimController(ClaimService claimService, UserRepository userRepository) {
        this.claimService = claimService;
        this.userRepository = userRepository;
    }

    // ---------------- Employee submits a new claim ----------------
    @PostMapping("/submit")
    public ResponseEntity<ClaimResponse> submitClaim(
            @RequestParam Long employeeId,
            @RequestParam Long policyId,
            @RequestParam Double amount,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile document
    ) throws IOException {

        String documentPath = null;
        if (document != null && !document.isEmpty()) {
            String extension = document.getOriginalFilename() != null ?
                    document.getOriginalFilename().substring(document.getOriginalFilename().lastIndexOf(".")) : "";
            String fileName = UUID.randomUUID() + extension;
            Path dirPath = Paths.get(uploadDir);
            File uploadFolder = dirPath.toFile();
            if (!uploadFolder.exists() && !uploadFolder.mkdirs()) {
                throw new IOException("Could not create upload directory: " + dirPath.toAbsolutePath());
            }
            Path filePath = dirPath.resolve(fileName);
            document.transferTo(filePath.toFile());
            documentPath = filePath.toAbsolutePath().toString();
        }

        ClaimRequest request = new ClaimRequest();
        request.setEmployeeId(employeeId);
        request.setPolicyId(policyId);
        request.setAmount(amount);
        request.setDescription(description);
        request.setDocumentPath(documentPath);

        return ResponseEntity.ok(claimService.submitClaim(request));
    }

    // ---------------- Fetch all claims (Admin/Agent) ----------------
    @GetMapping
    public ResponseEntity<List<ClaimResponse>> getAllClaims() {
        return ResponseEntity.ok(claimService.getAllClaims());
    }

    // ---------------- Fetch claims by employee ----------------
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ClaimResponse>> getClaimsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(claimService.getClaimsByEmployee(employeeId));
    }

    // ---------------- Fetch claims assigned to agent ----------------
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<ClaimResponse>> getClaimsByAgent(@PathVariable Long agentId) {
        return ResponseEntity.ok(claimService.getClaimsByAgent(agentId));
    }

    // ---------------- Assign agent to claim (Admin only) ----------------
    @PutMapping("/{claimId}/assign-agent/{agentId}")
    public ResponseEntity<ClaimResponse> assignAgent(
            @PathVariable Long claimId,
            @PathVariable Long agentId
    ) {
        Optional<ClaimResponse> response = claimService.assignAgent(claimId, agentId);
        return response.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // ---------------- Update claim status (Admin only) ----------------
    @PutMapping("/{claimId}/status")
    public ResponseEntity<ClaimResponse> updateClaimStatus(
            @PathVariable Long claimId,
            @RequestParam String status,
            @RequestParam Long adminId
    ) {
        return ResponseEntity.ok(claimService.updateClaimStatus(claimId, status, adminId));
    }

    // ---------------- Settle claim (Admin only) ----------------
    @PutMapping("/{claimId}/settle")
    public ResponseEntity<ClaimResponse> settleClaim(
            @PathVariable Long claimId,
            @RequestParam Double settlementAmount,
            @RequestParam(required = false) Long processedById,
            @RequestParam(required = false) String resolutionNotes
    ) {
        Long adminId = processedById != null ? processedById : null;
        return ResponseEntity.ok(claimService.settleClaim(claimId, settlementAmount, adminId, resolutionNotes));
    }

    // ---------------- Agent submits a suggestion ----------------
    @PutMapping("/{claimId}/agent-suggestion")
    public ResponseEntity<ClaimResponse> submitAgentSuggestion(
            @PathVariable Long claimId,
            @RequestParam Long agentId,
            @RequestParam String suggestion,
            @RequestParam(required = false) String notes
    ) {
        return ResponseEntity.ok(claimService.submitAgentSuggestion(claimId, agentId, suggestion, notes));
    }

    // ---------------- Employee adds additional files to claim ----------------
    @PostMapping("/{claimId}/add-files")
    public ResponseEntity<String> addFilesToClaim(
            @PathVariable Long claimId,
            @RequestParam("documents") MultipartFile[] documents,
            Principal principal
    ) {
        try {
            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            Claim claim = claimService.getClaimById(claimId);

            if (!claim.getEmployee().getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("You are not authorized to update this claim");
            }

            for (MultipartFile file : documents) {
                if (file != null && !file.isEmpty()) {
                    String extension = file.getOriginalFilename() != null ?
                            file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")) : "";
                    String fileName = UUID.randomUUID() + extension;

                    Path dirPath = Paths.get(uploadDir);
                    File uploadFolder = dirPath.toFile();
                    if (!uploadFolder.exists() && !uploadFolder.mkdirs()) {
                        throw new IOException("Could not create upload directory: " + dirPath.toAbsolutePath());
                    }

                    Path filePath = dirPath.resolve(fileName);
                    file.transferTo(filePath.toFile());

                    claimService.addFileToClaim(claimId, filePath.toAbsolutePath().toString());
                }
            }

            return ResponseEntity.ok("Files added successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to add files: " + e.getMessage());
        }
    }

    // ---------------- Fetch single claim ----------------
    @GetMapping("/{claimId}")
    public ResponseEntity<ClaimResponse> getClaim(@PathVariable Long claimId) {
        Claim claim = claimService.getClaimById(claimId);
        return ResponseEntity.ok(claimService.toResponse(claim));
    }

    // ---------------- Employee claim statistics ----------------
    @GetMapping("/statistics/employee/{employeeId}")
    public ResponseEntity<Map<String, Object>> getEmployeeStatistics(@PathVariable Long employeeId) {
        return ResponseEntity.ok(claimService.getEmployeeClaimStatistics(employeeId));
    }
}
