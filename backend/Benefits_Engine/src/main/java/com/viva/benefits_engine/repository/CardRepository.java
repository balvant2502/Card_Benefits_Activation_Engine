package com.viva.benefits_engine.repository;

import com.viva.benefits_engine.models.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByUserId(Long userId);
    List<Card> findByUserIdAndIsActiveTrue(Long userId);
}
