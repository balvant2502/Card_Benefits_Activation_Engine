package com.viva.benefits_engine.repository;

import com.viva.benefits_engine.models.Claim;
import com.viva.benefits_engine.models.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByUserId(Long userId);
    List<Claim> findByTransactionId(Long transactionId);
    List<Claim> findByStatus(ClaimStatus status);
    List<Claim> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByTransactionIdAndBenefitId(Long transactionId, Long benefitId);
    Long countByStatus(ClaimStatus status);
}
