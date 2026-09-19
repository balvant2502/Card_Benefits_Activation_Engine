package com.viva.benefits_engine.service;

import com.viva.benefits_engine.models.*;
import com.viva.benefits_engine.repository.ClaimRepository;
import com.viva.benefits_engine.repository.ClaimAuditRepository;
import com.viva.benefits_engine.repository.BenefitRepository;
import com.viva.benefits_engine.repository.ClaimNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClaimService {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ClaimAuditRepository claimAuditRepository;

    @Autowired
    private BenefitRepository benefitRepository;

    @Autowired
    private ClaimNotificationRepository claimNotificationRepository;

    @Autowired
    private TransactionService transactionService;

    public Claim createClaim(Long transactionId, Long benefitId, Long userId) {
        Optional<Transaction> transaction = transactionService.getTransactionById(transactionId);
        Optional<Benefit> benefit = benefitRepository.findById(benefitId);

        if (transaction.isEmpty() || benefit.isEmpty()) {
            return null;
        }

        if (transaction.get().getUser() == null || !transaction.get().getUser().getId().equals(userId)
                || claimRepository.existsByTransactionIdAndBenefitId(transactionId, benefitId)) {
            return null;
        }

        Claim claim = new Claim();
        claim.setTransaction(transaction.get());
        claim.setBenefit(benefit.get());
        claim.setUser(transaction.get().getUser());
        claim.setStatus(ClaimStatus.ELIGIBLE);

        // Pre-fill claim data from transaction
        String prefilledData = buildPrefilledData(transaction.get());
        claim.setPrefilledData(prefilledData);

        Claim savedClaim = claimRepository.save(claim);
        recordAudit(savedClaim, null, ClaimStatus.ELIGIBLE, "SYSTEM", "Claim created and eligible");
        createNotification(savedClaim);

        return savedClaim;
    }

    public Claim createEligibleClaim(Transaction transaction, Benefit benefit) {
        if (transaction == null || benefit == null
                || transaction.getUser() == null
                || claimRepository.existsByTransactionIdAndBenefitId(transaction.getId(), benefit.getId())) {
            return null;
        }

        Claim claim = new Claim();
        claim.setTransaction(transaction);
        claim.setBenefit(benefit);
        claim.setUser(transaction.getUser());
        claim.setStatus(ClaimStatus.ELIGIBLE);
        claim.setPrefilledData(buildPrefilledData(transaction));

        Claim savedClaim = claimRepository.save(claim);
        recordAudit(savedClaim, null, ClaimStatus.ELIGIBLE, "SYSTEM",
                "Eligible benefit detected for the card transaction");
        createNotification(savedClaim);
        return savedClaim;
    }

    private void createNotification(Claim claim) {
        ClaimNotification notification = new ClaimNotification();
        notification.setUser(claim.getUser());
        notification.setClaim(claim);
        notification.setMessage("You may claim " + claim.getBenefit().getName()
                + " for your " + claim.getTransaction().getMerchant() + " transaction.");
        claimNotificationRepository.save(notification);
    }

    private String buildPrefilledData(Transaction transaction) {
        return String.format(
                "{\"merchant\":\"%s\",\"amount\":%s,\"date\":\"%s\",\"category\":\"%s\"}",
                transaction.getMerchant(),
                transaction.getAmount(),
                transaction.getTxnDate(),
                transaction.getCategory()
        );
    }

    public Claim activateClaim(Long claimId) {
        Optional<Claim> claim = claimRepository.findById(claimId);
        if (claim.isPresent()) {
            Claim c = claim.get();
            ClaimStatus oldStatus = c.getStatus();
            c.setStatus(ClaimStatus.ACTIVATED);
            Claim updated = claimRepository.save(c);
            recordAudit(updated, oldStatus, ClaimStatus.ACTIVATED, "CUSTOMER", "Claim activated by customer");
            return updated;
        }
        return null;
    }

    public Claim submitClaim(Long claimId, String submissionData) {
        Optional<Claim> claim = claimRepository.findById(claimId);
        if (claim.isPresent()) {
            Claim c = claim.get();
            ClaimStatus oldStatus = c.getStatus();
            c.setSubmissionData(submissionData);
            c.setStatus(ClaimStatus.SUBMITTED);
            Claim updated = claimRepository.save(c);
            recordAudit(updated, oldStatus, ClaimStatus.SUBMITTED, "CUSTOMER", "Claim submitted for review");
            return updated;
        }
        return null;
    }

    public Claim approveClaim(Long claimId, String adminNotes) {
        Optional<Claim> claim = claimRepository.findById(claimId);
        if (claim.isPresent()) {
            Claim c = claim.get();
            ClaimStatus oldStatus = c.getStatus();
            c.setStatus(ClaimStatus.APPROVED);
            c.setAdminNotes(adminNotes);
            c.setReviewedAt(LocalDateTime.now());
            Claim updated = claimRepository.save(c);
            recordAudit(updated, oldStatus, ClaimStatus.APPROVED, "ADMIN", adminNotes);
            return updated;
        }
        return null;
    }

    public Claim rejectClaim(Long claimId, String rejectionReason) {
        Optional<Claim> claim = claimRepository.findById(claimId);
        if (claim.isPresent()) {
            Claim c = claim.get();
            ClaimStatus oldStatus = c.getStatus();
            c.setStatus(ClaimStatus.REJECTED);
            c.setAdminNotes(rejectionReason);
            c.setReviewedAt(LocalDateTime.now());
            Claim updated = claimRepository.save(c);
            recordAudit(updated, oldStatus, ClaimStatus.REJECTED, "ADMIN", rejectionReason);
            return updated;
        }
        return null;
    }

    private void recordAudit(Claim claim, ClaimStatus oldStatus, ClaimStatus newStatus, String actionBy, String remarks) {
        ClaimAudit audit = new ClaimAudit();
        audit.setClaim(claim);
        audit.setStatusFrom(oldStatus);
        audit.setStatusTo(newStatus);
        audit.setActionBy(actionBy);
        audit.setRemarks(remarks);
        claimAuditRepository.save(audit);
    }

    public Optional<Claim> getClaimById(Long id) {
        return claimRepository.findById(id);
    }

    public List<Claim> getUserClaims(Long userId) {
        return claimRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Claim> getClaimsByStatus(ClaimStatus status) {
        return claimRepository.findByStatus(status);
    }

    public List<ClaimAudit> getClaimHistory(Long claimId) {
        return claimAuditRepository.findByClaimIdOrderByCreatedAtDesc(claimId);
    }

    public long getPendingClaimCount() {
        return claimRepository.countByStatus(ClaimStatus.SUBMITTED);
    }

    public long getApprovedClaimCount() {
        return claimRepository.countByStatus(ClaimStatus.APPROVED);
    }

    public long getRejectedClaimCount() {
        return claimRepository.countByStatus(ClaimStatus.REJECTED);
    }
}
