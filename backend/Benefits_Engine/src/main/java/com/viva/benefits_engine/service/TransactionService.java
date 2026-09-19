package com.viva.benefits_engine.service;

import com.viva.benefits_engine.models.Transaction;
import com.viva.benefits_engine.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

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
