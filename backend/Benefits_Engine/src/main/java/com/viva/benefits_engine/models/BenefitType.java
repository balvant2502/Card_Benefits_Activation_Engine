package com.viva.benefits_engine.models;

public enum BenefitType {
    PURCHASE_PROTECTION("Purchase Protection"),
    RETURN_PROTECTION("Return Protection"),
    TRAVEL_DELAY_INSURANCE("Travel-Delay Insurance");

    private final String displayName;

    BenefitType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
