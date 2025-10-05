package com.insurai.backend.repository;

import com.insurai.backend.entity.ClaimProgressNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClaimProgressNoteRepository extends JpaRepository<ClaimProgressNote, Long> {
    // Fetch notes by Claim ID directly
    List<ClaimProgressNote> findByClaim_Id(Long claimId);
}
