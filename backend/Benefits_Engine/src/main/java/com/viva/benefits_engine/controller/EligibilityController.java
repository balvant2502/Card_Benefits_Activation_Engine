package com.viva.benefits_engine.controller;

import com.viva.benefits_engine.models.Transaction;
import com.viva.benefits_engine.service.RulesEngineService;
import com.viva.benefits_engine.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/eligibility")
@CrossOrigin(origins = "*")
public class EligibilityController {

    @Autowired
    private RulesEngineService rulesEngineService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/{txnId}")
    public ResponseEntity<Map<String, Object>> checkEligibility(@PathVariable Long txnId) {
        Optional<Transaction> transaction = transactionService.getTransactionById(txnId);

        if (transaction.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RulesEngineService.EligibilityResult result = rulesEngineService.checkEligibility(transaction.get());

        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", txnId);
        response.put("isEligible", result.isEligible());
        response.put("benefitId", result.getBenefitId());
        response.put("benefitName", result.getBenefitName());
        response.put("reason", result.getReason());

        if (result.isEligible()) {
            Transaction txn = transaction.get();
            txn.setIsEligible(true);
            txn.setEligibleBenefitName(result.getBenefitName());
            txn.setEligibilityReason(result.getReason());
            transactionService.updateTransaction(txn);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkEligibilityBatch(@RequestBody Transaction transaction) {
        RulesEngineService.EligibilityResult result = rulesEngineService.checkEligibility(transaction);

        Map<String, Object> response = new HashMap<>();
        response.put("isEligible", result.isEligible());
        response.put("benefitId", result.getBenefitId());
        response.put("benefitName", result.getBenefitName());
        response.put("reason", result.getReason());

        return ResponseEntity.ok(response);
    }
}
