package com.viva.benefits_engine.repository;

import com.viva.benefits_engine.models.BenefitRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BenefitRuleRepository extends JpaRepository<BenefitRule, Long> {
    List<BenefitRule> findByIsActiveTrueOrderByPriorityDesc();
    List<BenefitRule> findByCategory(String category);
    List<BenefitRule> findByBenefitId(Long benefitId);
}
