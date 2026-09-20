package com.viva.benefits_engine.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "benefit_rules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BenefitRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "benefit_id", nullable = false)
    private Benefit benefit;

    @ManyToOne
    @JoinColumn(name = "card_benefit_id")
    private CardBenefit cardBenefit;

    // Rule condition: category
    private String category;

    // Rule condition: minimum amount
    private BigDecimal minAmount;

    // Rule condition: maximum amount (optional, null = no max)
    private BigDecimal maxAmount;

    // Keyword match in merchant name (optional, comma separated)
    private String merchantKeywords;

    // Card product conditions. A blank value matches any card.
    private String cardType;
    private String cardNetwork;

    // Priority for rule matching (higher = evaluated first)
    private Integer priority = 0;

    private Boolean isActive = true;

    // Description of the rule
    @Column(length = 500)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
