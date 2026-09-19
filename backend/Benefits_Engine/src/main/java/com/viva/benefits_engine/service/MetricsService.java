package com.viva.benefits_engine.service;

import com.viva.benefits_engine.models.*;
import com.viva.benefits_engine.repository.MetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class MetricsService {

    @Autowired
    private MetricsRepository metricsRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ClaimService claimService;

    public com.viva.benefits_engine.models.Metrics recordMetrics(BigDecimal detectionAccuracy, BigDecimal prefillQuality) {
        Metrics metrics = new com.viva.benefits_engine.models.Metrics();
        metrics.setDetectionAccuracy(detectionAccuracy);
        metrics.setPrefillQuality(prefillQuality);
        metrics.setUnclaimedBenefitCount(getUnclaimedBenefitCount());
        metrics.setTotalEligibleCount(getTotalEligibleCount());
        metrics.setTotalClaimsActivated(getTotalClaimsActivated());
        metrics.setTotalClaimsApproved(claimService.getApprovedClaimCount());
        metrics.setAvgClaimProcessingTime(calculateAvgProcessingTime());
        metrics.setRecordedAt(LocalDateTime.now());

        return metricsRepository.save(metrics);
    }

    private Long getUnclaimedBenefitCount() {
        long totalEligible = getTotalEligibleCount();
        long totalClaims = getTotalClaimsActivated();
        return Math.max(totalEligible - totalClaims, 0L);
    }

    private Long getTotalEligibleCount() {
        return transactionService.getEligibleTransactionCount();
    }

    private Long getTotalClaimsActivated() {
        return (long) claimService.getClaimsByStatus(ClaimStatus.ACTIVATED).size();
    }

    private BigDecimal calculateAvgProcessingTime() {
        // Simplified: avg time from SUBMITTED to APPROVED/REJECTED
        long approvedCount = claimService.getApprovedClaimCount();
        long rejectedCount = claimService.getRejectedClaimCount();

        if (approvedCount + rejectedCount == 0) {
            return BigDecimal.ZERO;
        }

        // Mock: assuming avg 24 hours per claim
        return new BigDecimal(24);
    }

    public Metrics getLatestMetrics() {
        return metricsRepository.findAll().stream()
                .max((m1, m2) -> m1.getRecordedAt().compareTo(m2.getRecordedAt()))
                .orElse(null);
    }
}
