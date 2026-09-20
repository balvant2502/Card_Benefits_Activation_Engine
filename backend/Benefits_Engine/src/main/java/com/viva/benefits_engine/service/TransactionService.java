package com.viva.benefits_engine.service;

import com.viva.benefits_engine.models.Transaction;
import com.viva.benefits_engine.repository.TransactionRepository;
import com.viva.benefits_engine.repository.CardRepository;
import com.viva.benefits_engine.repository.UserRepository;
import com.viva.benefits_engine.models.Card;
import com.viva.benefits_engine.models.User;
import com.viva.benefits_engine.dto.DummySwipeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    public Transaction createDummySwipe(String email, DummySwipeRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Card card = request != null && request.getCardId() != null
                ? cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new IllegalArgumentException("Card not found"))
                : cardRepository.findByUserIdAndIsActiveTrue(user.getId()).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No active card found"));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Card does not belong to the authenticated user");
        }
        if (!Boolean.TRUE.equals(card.getIsActive())) {
            throw new IllegalArgumentException("Card is inactive");
        }

        List<String> merchants = List.of("Amazon", "Flipkart", "Reliance Retail", "IndiGo", "MakeMyTrip");
        List<String> categories = List.of("ELECTRONICS", "RETAIL", "RETAIL", "AIRLINE", "HOTEL");
        int index = ThreadLocalRandom.current().nextInt(merchants.size());

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setCard(card);
        transaction.setMerchant(merchants.get(index));
        transaction.setCategory(categories.get(index));
        transaction.setAmount(BigDecimal.valueOf(
                ThreadLocalRandom.current().nextInt(1000, 25001)));
        transaction.setTxnDate(LocalDateTime.now());
        transaction.setIsEligible(false);
        return transactionRepository.save(transaction);
    }

    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    public List<Transaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUserIdOrderByTxnDateDesc(userId);
    }

    public List<Transaction> getCardTransactions(Long cardId) {
        return transactionRepository.findByCardId(cardId);
    }

    public List<Transaction> getEligibleTransactions() {
        return transactionRepository.findByIsEligibleTrue();
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction updateTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }

    public long getTotalTransactionCount() {
        return transactionRepository.count();
    }

    public long getEligibleTransactionCount() {
        return transactionRepository.findByIsEligibleTrue().size();
    }
}
