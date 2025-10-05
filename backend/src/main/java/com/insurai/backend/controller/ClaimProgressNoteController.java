package com.insurai.backend.controller;

import com.insurai.backend.dto.ClaimProgressNoteDTO;
import com.insurai.backend.dto.AddClaimNoteRequest;
import com.insurai.backend.entity.Claim;
import com.insurai.backend.entity.Employee;
import com.insurai.backend.entity.Role;
import com.insurai.backend.entity.User;
import com.insurai.backend.repository.EmployeeRepository;
import com.insurai.backend.repository.UserRepository;
import com.insurai.backend.service.ClaimProgressNoteService;
import com.insurai.backend.service.ClaimService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/claim-notes")
public class ClaimProgressNoteController {

    private final ClaimProgressNoteService noteService;
    private final ClaimService claimService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    private static final Logger logger = LoggerFactory.getLogger(ClaimProgressNoteController.class);

    public ClaimProgressNoteController(ClaimProgressNoteService noteService,
                                       ClaimService claimService,
                                       UserRepository userRepository,
                                       EmployeeRepository employeeRepository) {
        this.noteService = noteService;
        this.claimService = claimService;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping("/add")
    public ClaimProgressNoteDTO addNote(@RequestBody AddClaimNoteRequest request) {
        return noteService.addNote(
                request.getClaimId(),
                request.getAgentId(),
                request.getNote()
        );
    }

    @GetMapping("/claim/{claimId}")
    public ResponseEntity<List<ClaimProgressNoteDTO>> getNotes(@PathVariable Long claimId, Principal principal) {
        Claim claim = claimService.getClaimById(claimId);
        logger.info("Fetching notes for claim ID: {}", claimId);

        if (principal != null) {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));
            logger.info("Current user: {} (Role: {})", currentUser.getUsername(), currentUser.getRole());

            if (currentUser.getRole() == Role.EMPLOYEE) {
                Employee employee = employeeRepository.findByUserId(currentUser.getId())
                        .orElseThrow(() -> new RuntimeException("Employee not found for user ID " + currentUser.getId()));

                if (!claim.getEmployee().getId().equals(employee.getId())) {
                    logger.warn("Access denied: user {} trying to access claim {}", currentUser.getId(), claimId);
                    return ResponseEntity.status(403).build();
                }
            }
        }

        List<ClaimProgressNoteDTO> notes = noteService.getNotesByClaim(claimId);
        logger.info("Found {} notes for claim ID {}", notes.size(), claimId);
        return ResponseEntity.ok(notes);
    }
}
