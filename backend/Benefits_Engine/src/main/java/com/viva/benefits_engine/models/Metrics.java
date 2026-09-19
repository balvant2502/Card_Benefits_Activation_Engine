package com.viva.benefits_engine.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "metrics")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Metrics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Detection accuracy: correct detections / total detections
    private BigDecimal detectionAccuracy;

    // Pre-fill quality: fields correctly prefilled / total fields
    private BigDecimal prefillQuality;

    // Unclaimed benefits count
    private Long unclaimedBenefitCount;

    // Total eligible benefits detected
    private Long totalEligibleCount;

    // Total claims activated
    private Long totalClaimsActivated;

    // Total claims approved
    private Long totalClaimsApproved;

    // Average claim processing time (in hours)
    private BigDecimal avgClaimProcessingTime;

    // Recorded timestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();
}
