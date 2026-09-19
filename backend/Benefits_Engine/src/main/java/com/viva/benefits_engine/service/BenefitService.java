package com.viva.benefits_engine.service;

import com.viva.benefits_engine.models.Benefit;
import com.viva.benefits_engine.repository.BenefitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BenefitService {

    @Autowired
    private BenefitRepository benefitRepository;

    public Benefit saveBenefit(Benefit benefit) {
        return benefitRepository.save(benefit);
    }

    public Optional<Benefit> getBenefitById(Long id) {
        return benefitRepository.findById(id);
    }

    public List<Benefit> getActiveBenefits() {
        return benefitRepository.findByIsActiveTrue();
    }

    public List<Benefit> getAllBenefits() {
        return benefitRepository.findAll();
    }

    public Benefit updateBenefit(Benefit benefit) {
        return benefitRepository.save(benefit);
    }

    public void deleteBenefit(Long id) {
        benefitRepository.deleteById(id);
    }

    public long getBenefitCount() {
        return benefitRepository.count();
    }
}
