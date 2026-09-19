package com.viva.benefits_engine.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "benefits")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Benefit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    // Comma separated categories, e.g. "AIRLINE,HOTEL"
    private String applicableCategories;

    private BigDecimal minAmount;

    // Comma separated fields needed in the claim form
    @Column(length = 500)
    private String requiredClaimFields;

    // Benefit type
    @Enumerated(EnumType.STRING)
    private BenefitType benefitType;

    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}