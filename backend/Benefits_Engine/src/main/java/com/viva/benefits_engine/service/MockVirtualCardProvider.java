package com.viva.benefits_engine.service;

import com.viva.benefits_engine.models.Card;
import com.viva.benefits_engine.models.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockVirtualCardProvider implements VirtualCardProvider {

    @Override
    public Card createCard(User user) {
        String providerCardId = UUID.randomUUID().toString();

        Card card = new Card();
        card.setUser(user);
        card.setCardNumberLast4(providerCardId.substring(providerCardId.length() - 4));
        card.setCardType("VIRTUAL_DEMO");
        card.setCardNetwork("VISA");
        card.setIsActive(true);
        return card;
    }
}
