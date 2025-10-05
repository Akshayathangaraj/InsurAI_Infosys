package com.insurai.backend.service;

import com.insurai.backend.dto.ClaimProgressNoteDTO;
import com.insurai.backend.entity.Claim;
import com.insurai.backend.entity.ClaimProgressNote;
import com.insurai.backend.entity.User;
import com.insurai.backend.repository.ClaimProgressNoteRepository;
import com.insurai.backend.repository.ClaimRepository;
import com.insurai.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClaimProgressNoteService {

    private final ClaimProgressNoteRepository noteRepository;
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;

    public ClaimProgressNoteService(ClaimProgressNoteRepository noteRepository,
                                    ClaimRepository claimRepository,
                                    UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.claimRepository = claimRepository;
        this.userRepository = userRepository;
    }

    public ClaimProgressNoteDTO addNote(Long claimId, Long agentId, String note) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        ClaimProgressNote cn = new ClaimProgressNote();
        cn.setClaim(claim);
        if (agentId != null) {
            User agent = userRepository.findById(agentId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            cn.setAgent(agent);
        } else {
            cn.setAgent(null);
        }
        cn.setNote(note);

        ClaimProgressNote saved = noteRepository.save(cn);

        ClaimProgressNoteDTO dto = new ClaimProgressNoteDTO();
        dto.setId(saved.getId());
        dto.setClaimId(saved.getClaim().getId());
        dto.setAgentId(saved.getAgent() != null ? saved.getAgent().getId() : null);
        dto.setAgentName(saved.getAgent() != null ? saved.getAgent().getUsername() : "System");
        dto.setNote(saved.getNote());
        dto.setCreatedAt(saved.getCreatedAt());
        return dto;
    }

    public List<ClaimProgressNoteDTO> getNotesByClaim(Long claimId) {
        // ✅ Correct repository method
        List<ClaimProgressNote> notes = noteRepository.findByClaim_Id(claimId);

        return notes.stream().map(note -> {
            ClaimProgressNoteDTO dto = new ClaimProgressNoteDTO();
            dto.setId(note.getId());
            dto.setClaimId(note.getClaim().getId());
            dto.setAgentId(note.getAgent() != null ? note.getAgent().getId() : null);
            dto.setAgentName(note.getAgent() != null ? note.getAgent().getUsername() : "System");
            dto.setNote(note.getNote());
            dto.setCreatedAt(note.getCreatedAt());
            return dto;
        }).toList();
    }
}
