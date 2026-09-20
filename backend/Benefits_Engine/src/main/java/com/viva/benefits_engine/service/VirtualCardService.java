package com.viva.benefits_engine.service;

import com.viva.benefits_engine.dto.VirtualCardResponse;
import com.viva.benefits_engine.models.Card;
import com.viva.benefits_engine.models.User;
import com.viva.benefits_engine.repository.CardRepository;
import com.viva.benefits_engine.repository.UserRepository;
import com.viva.benefits_engine.repository.CardBenefitRepository;
import com.viva.benefits_engine.repository.BenefitRepository;
import com.viva.benefits_engine.models.Benefit;
import com.viva.benefits_engine.models.CardBenefit;
import com.viva.benefits_engine.models.BenefitRule;
import com.viva.benefits_engine.repository.BenefitRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VirtualCardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final VirtualCardProvider virtualCardProvider;
    private final CardBenefitRepository cardBenefitRepository;
    private final BenefitRepository benefitRepository;
    private final BenefitRuleRepository benefitRuleRepository;

    public VirtualCardService(
            CardRepository cardRepository,
            UserRepository userRepository,
            VirtualCardProvider virtualCardProvider,
            CardBenefitRepository cardBenefitRepository,
            BenefitRepository benefitRepository,
            BenefitRuleRepository benefitRuleRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.virtualCardProvider = virtualCardProvider;
        this.cardBenefitRepository = cardBenefitRepository;
        this.benefitRepository = benefitRepository;
        this.benefitRuleRepository = benefitRuleRepository;
    }

    public Card createInitialCard(User user) {
        return cardRepository.save(virtualCardProvider.createCard(user));
    }

    public List<VirtualCardResponse> getCardsForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return cardRepository.findByUserId(user.getId()).stream()
                .map(card -> new VirtualCardResponse(card,
                        cardBenefitRepository.findByCardIdAndIsActiveTrue(card.getId())))
                .toList();
    }

    public CardBenefit assignBenefit(Long cardId, Long benefitId) {
        if (cardBenefitRepository.existsByCardIdAndBenefitId(cardId, benefitId)) {
            throw new IllegalArgumentException("Benefit is already assigned to this card");
        }
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
        Benefit benefit = benefitRepository.findById(benefitId)
                .orElseThrow(() -> new IllegalArgumentException("Benefit not found"));
        if (benefit.getBenefitType() == null) {
            throw new IllegalArgumentException("Benefit must use one of the supported protection types");
        }
        CardBenefit cardBenefit = new CardBenefit();
        cardBenefit.setCard(card);
        cardBenefit.setBenefit(benefit);
        cardBenefit.setIsActive(true);
        return cardBenefitRepository.save(cardBenefit);
    }

    public BenefitRule createRule(Long cardId, Long benefitId, BenefitRule rule) {
        CardBenefit cardBenefit = cardBenefitRepository.findByCardIdAndBenefitId(cardId, benefitId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Benefit must be assigned to the card before adding a rule"));
        rule.setId(null);
        rule.setCardBenefit(cardBenefit);
        rule.setBenefit(cardBenefit.getBenefit());
        rule.setIsActive(true);
        return benefitRuleRepository.save(rule);
    }
}
