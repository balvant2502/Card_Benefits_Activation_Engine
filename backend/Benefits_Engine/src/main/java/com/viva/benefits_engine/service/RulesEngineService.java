package com.viva.benefits_engine.service;

import com.viva.benefits_engine.models.*;
import com.viva.benefits_engine.repository.BenefitRepository;
import com.viva.benefits_engine.repository.BenefitRuleRepository;
import com.viva.benefits_engine.repository.CardBenefitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class RulesEngineService {

    @Autowired
    private BenefitRuleRepository benefitRuleRepository;

    @Autowired
    private BenefitRepository benefitRepository;

    @Autowired
    private CardBenefitRepository cardBenefitRepository;

    public EligibilityResult checkEligibility(Transaction transaction) {
        if (transaction.getCard() == null) {
            return new EligibilityResult(false, null, null, "Transaction has no card");
        }
        List<BenefitRule> rules = benefitRuleRepository
                .findByCardBenefitCardIdAndIsActiveTrueOrderByPriorityDesc(transaction.getCard().getId());

        for (BenefitRule rule : rules) {
            if (isRuleMatched(transaction, rule)) {
                Benefit benefit = rule.getBenefit();
                String reason = buildReason(rule, transaction);
                return new EligibilityResult(true, benefit.getId(), benefit.getName(), reason);
            }
        }

        return new EligibilityResult(false, null, null, "No matching benefit rule found");
    }

    private boolean isRuleMatched(Transaction transaction, BenefitRule rule) {
        if (transaction.getCard() == null || !Boolean.TRUE.equals(transaction.getCard().getIsActive())) {
            return false;
        }

        if (transaction.getUser() == null || transaction.getCard().getUser() == null
                || !transaction.getCard().getUser().getId().equals(transaction.getUser().getId())) {
            return false;
        }

        if (!isValueMatched(transaction.getCard().getCardType(), rule.getCardType())
                || !isValueMatched(transaction.getCard().getCardNetwork(), rule.getCardNetwork())) {
            return false;
        }

        // Check category
        if (!isCategoryMatched(transaction.getCategory(), rule.getCategory())) {
            return false;
        }

        // Check amount range
        if (rule.getMinAmount() != null && transaction.getAmount().compareTo(rule.getMinAmount()) < 0) {
            return false;
        }

        if (rule.getMaxAmount() != null && transaction.getAmount().compareTo(rule.getMaxAmount()) > 0) {
            return false;
        }

        // Check merchant keywords (optional)
        if (rule.getMerchantKeywords() != null && !rule.getMerchantKeywords().isBlank()) {
            if (!isMerchantMatched(transaction.getMerchant(), rule.getMerchantKeywords())) {
                return false;
            }
        }

        return true;
    }

    private boolean isValueMatched(String actualValue, String configuredValue) {
        return configuredValue == null || configuredValue.isBlank()
                || (actualValue != null && actualValue.equalsIgnoreCase(configuredValue.trim()));
    }

    private boolean isCategoryMatched(String txnCategory, String ruleCategory) {
        if (ruleCategory == null || ruleCategory.isBlank()) {
            return true;
        }
        String[] categories = ruleCategory.split(",");
        for (String cat : categories) {
            if (cat.trim().equalsIgnoreCase(txnCategory)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMerchantMatched(String merchant, String keywords) {
        String[] keywordList = keywords.split(",");
        for (String keyword : keywordList) {
            if (merchant.toLowerCase().contains(keyword.toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }

    private String buildReason(BenefitRule rule, Transaction transaction) {
        return String.format("%s qualifies for %s (Amount: %s, Category: %s)",
                transaction.getMerchant(),
                rule.getBenefit().getName(),
                transaction.getAmount(),
                transaction.getCategory());
    }

    public Benefit getBenefitById(Long id) {
        Optional<Benefit> benefit = benefitRepository.findById(id);
        return benefit.orElse(null);
    }

    public List<Benefit> getActiveBenefits() {
        return benefitRepository.findByIsActiveTrue();
    }

    public List<BenefitRule> getAllRules() {
        return benefitRuleRepository.findByIsActiveTrueOrderByPriorityDesc();
    }

    public static class EligibilityResult {
        public boolean isEligible;
        public Long benefitId;
        public String benefitName;
        public String reason;

        public EligibilityResult(boolean isEligible, Long benefitId, String benefitName, String reason) {
            this.isEligible = isEligible;
            this.benefitId = benefitId;
            this.benefitName = benefitName;
            this.reason = reason;
        }

        public boolean isEligible() {
            return isEligible;
        }

        public Long getBenefitId() {
            return benefitId;
        }

        public String getBenefitName() {
            return benefitName;
        }

        public String getReason() {
            return reason;
        }
    }
}
