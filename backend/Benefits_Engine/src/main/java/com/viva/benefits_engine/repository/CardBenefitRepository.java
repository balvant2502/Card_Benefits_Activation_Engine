package com.viva.benefits_engine.repository;

import com.viva.benefits_engine.models.CardBenefit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CardBenefitRepository extends JpaRepository<CardBenefit, Long> {
    List<CardBenefit> findByCardIdAndIsActiveTrue(Long cardId);
    boolean existsByCardIdAndBenefitId(Long cardId, Long benefitId);
    Optional<CardBenefit> findByCardIdAndBenefitId(Long cardId, Long benefitId);
}
