package com.viva.benefits_engine.service;

import com.viva.benefits_engine.models.Card;
import com.viva.benefits_engine.models.User;

public interface VirtualCardProvider {
    Card createCard(User user);
}
