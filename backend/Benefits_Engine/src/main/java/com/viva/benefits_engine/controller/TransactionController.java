package com.viva.benefits_engine.controller;

import com.viva.benefits_engine.models.Transaction;
import com.viva.benefits_engine.service.TransactionService;
import com.viva.benefits_engine.service.ClaimService;
import com.viva.benefits_engine.service.RulesEngineService;
import com.viva.benefits_engine.dto.DummySwipeRequest;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private RulesEngineService rulesEngineService;

    @Autowired
    private ClaimService claimService;

    @PostMapping("/dummy-swipe")
    public ResponseEntity<Transaction> dummySwipe(
            @RequestBody(required = false) DummySwipeRequest request,
            Authentication authentication) {
        Transaction transaction = transactionService.createDummySwipe(
                authentication.getName(), request);
        return ResponseEntity.ok(processEligibility(transaction));
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        Transaction saved = transactionService.saveTransaction(transaction);
        return ResponseEntity.ok(processEligibility(saved));
    }

    private Transaction processEligibility(Transaction saved) {
        RulesEngineService.EligibilityResult result = rulesEngineService.checkEligibility(saved);
        if (result.isEligible()) {
            claimService.createEligibleClaim(saved, rulesEngineService.getBenefitById(result.getBenefitId()));
            saved.setIsEligible(true);
            saved.setEligibleBenefitName(result.getBenefitName());
            saved.setEligibilityReason(result.getReason());
            saved = transactionService.updateTransaction(saved);
        }
        return saved;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        Optional<Transaction> transaction = transactionService.getTransactionById(id);
        return transaction.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getUserTransactions(@PathVariable Long userId) {
        List<Transaction> transactions = transactionService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<Transaction>> getCardTransactions(@PathVariable Long cardId) {
        List<Transaction> transactions = transactionService.getCardTransactions(cardId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/eligible")
    public ResponseEntity<List<Transaction>> getEligibleTransactions() {
        List<Transaction> transactions = transactionService.getEligibleTransactions();
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody Transaction transaction) {
        Optional<Transaction> existing = transactionService.getTransactionById(id);
        if (existing.isPresent()) {
            transaction.setId(id);
            Transaction updated = transactionService.updateTransaction(transaction);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/count")
    public ResponseEntity<Long> getTotalCount() {
        return ResponseEntity.ok(transactionService.getTotalTransactionCount());
    }

    @GetMapping("/stats/eligible-count")
    public ResponseEntity<Long> getEligibleCount() {
        return ResponseEntity.ok(transactionService.getEligibleTransactionCount());
    }
}
