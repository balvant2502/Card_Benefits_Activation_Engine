package com.viva.benefits_engine.controller;

import com.viva.benefits_engine.models.Benefit;
import com.viva.benefits_engine.service.BenefitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/benefits")
@CrossOrigin(origins = "*")
public class BenefitController {

    @Autowired
    private BenefitService benefitService;

    @PostMapping
    public ResponseEntity<Benefit> createBenefit(@RequestBody Benefit benefit) {
        Benefit saved = benefitService.saveBenefit(benefit);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Benefit> getBenefit(@PathVariable Long id) {
        Optional<Benefit> benefit = benefitService.getBenefitById(id);
        return benefit.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Benefit>> getAllBenefits() {
        List<Benefit> benefits = benefitService.getAllBenefits();
        return ResponseEntity.ok(benefits);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Benefit>> getActiveBenefits() {
        List<Benefit> benefits = benefitService.getActiveBenefits();
        return ResponseEntity.ok(benefits);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Benefit> updateBenefit(@PathVariable Long id, @RequestBody Benefit benefit) {
        Optional<Benefit> existing = benefitService.getBenefitById(id);
        if (existing.isPresent()) {
            benefit.setId(id);
            Benefit updated = benefitService.updateBenefit(benefit);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBenefit(@PathVariable Long id) {
        benefitService.deleteBenefit(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/count")
    public ResponseEntity<Long> getBenefitCount() {
        return ResponseEntity.ok(benefitService.getBenefitCount());
    }
}
