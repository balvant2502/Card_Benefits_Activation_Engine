package com.viva.benefits_engine.repository;

import com.viva.benefits_engine.models.ClaimAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimAuditRepository extends JpaRepository<ClaimAudit, Long> {
    List<ClaimAudit> findByClaimIdOrderByCreatedAtDesc(Long claimId);
}
