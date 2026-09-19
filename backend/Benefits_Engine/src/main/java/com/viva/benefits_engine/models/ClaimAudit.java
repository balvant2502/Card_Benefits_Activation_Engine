package com.viva.benefits_engine.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim_audit")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ClaimAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Enumerated(EnumType.STRING)
    private ClaimStatus statusFrom;

    @Enumerated(EnumType.STRING)
    private ClaimStatus statusTo;

    private String actionBy;  // USER, ADMIN, SYSTEM

    @Column(length = 500)
    private String remarks;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
