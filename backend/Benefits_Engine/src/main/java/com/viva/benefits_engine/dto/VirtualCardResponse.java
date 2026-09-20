package com.viva.benefits_engine.dto;

import com.viva.benefits_engine.models.Card;
import com.viva.benefits_engine.models.CardBenefit;
import lombok.Getter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class VirtualCardResponse {
    private final Long id;
    private final String last4;
    private final String cardType;
    private final String network;
    private final Boolean active;
    private final Boolean virtual;
    private final String provider;
    private final List<String> benefits;

    public VirtualCardResponse(Card card, List<CardBenefit> cardBenefits) {
        this.id = card.getId();
        this.last4 = card.getCardNumberLast4();
        this.cardType = card.getCardType();
        this.network = card.getCardNetwork();
        this.active = card.getIsActive();
        this.virtual = "VIRTUAL_DEMO".equals(card.getCardType());
        this.provider = virtual ? "MOCK" : "UNKNOWN";
        this.benefits = cardBenefits.stream()
                .map(cardBenefit -> cardBenefit.getBenefit().getBenefitType().getDisplayName())
                .collect(Collectors.toList());
    }
}
