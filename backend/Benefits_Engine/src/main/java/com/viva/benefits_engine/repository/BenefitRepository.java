package com.viva.benefits_engine.repository;

import com.viva.benefits_engine.models.Benefit;
import com.viva.benefits_engine.models.BenefitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BenefitRepository extends JpaRepository<Benefit, Long> {
    List<Benefit> findByIsActiveTrue();
    Optional<Benefit> findByNameAndIsActiveTrue(String name);
    List<Benefit> findByBenefitType(BenefitType benefitType);
}
