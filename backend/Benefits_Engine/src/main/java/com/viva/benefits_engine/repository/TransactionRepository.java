package com.viva.benefits_engine.repository;

import com.viva.benefits_engine.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByCardId(Long cardId);
    List<Transaction> findByUserIdOrderByTxnDateDesc(Long userId);
    List<Transaction> findByIsEligibleTrue();
}
