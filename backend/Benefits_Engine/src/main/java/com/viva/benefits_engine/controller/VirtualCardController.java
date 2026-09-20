package com.viva.benefits_engine.controller;

import com.viva.benefits_engine.dto.VirtualCardResponse;
import com.viva.benefits_engine.service.VirtualCardService;
import com.viva.benefits_engine.models.CardBenefit;
import com.viva.benefits_engine.models.BenefitRule;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "*")
public class VirtualCardController {
    private final VirtualCardService virtualCardService;

    public VirtualCardController(VirtualCardService virtualCardService) {
        this.virtualCardService = virtualCardService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<VirtualCardResponse>> getMyCards(Authentication authentication) {
        return ResponseEntity.ok(virtualCardService.getCardsForUser(authentication.getName()));
    }

    @PostMapping("/me")
    public ResponseEntity<VirtualCardResponse> createMyCard(Authentication authentication) {
        return ResponseEntity.ok(virtualCardService.createCardForUser(authentication.getName()));
    }

    @PostMapping("/{cardId}/benefits/{benefitId}")
    public ResponseEntity<CardBenefit> assignBenefit(
            @PathVariable Long cardId, @PathVariable Long benefitId) {
        return ResponseEntity.ok(virtualCardService.assignBenefit(cardId, benefitId));
    }

    @PostMapping("/{cardId}/benefits/{benefitId}/rules")
    public ResponseEntity<BenefitRule> createRule(
            @PathVariable Long cardId,
            @PathVariable Long benefitId,
            @RequestBody BenefitRule rule) {
        return ResponseEntity.ok(virtualCardService.createRule(cardId, benefitId, rule));
    }
}
