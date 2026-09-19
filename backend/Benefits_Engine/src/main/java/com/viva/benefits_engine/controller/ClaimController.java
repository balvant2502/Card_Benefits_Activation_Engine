package com.viva.benefits_engine.controller;

import com.viva.benefits_engine.models.Claim;
import com.viva.benefits_engine.models.ClaimStatus;
import com.viva.benefits_engine.models.ClaimAudit;
import com.viva.benefits_engine.service.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = "*")
public class ClaimController {

    @Autowired
    private ClaimService claimService;

    @PostMapping
    public ResponseEntity<Claim> createClaim(@RequestParam Long transactionId, @RequestParam Long benefitId, @RequestParam Long userId) {
        Claim claim = claimService.createClaim(transactionId, benefitId, userId);
        if (claim != null) {
            return ResponseEntity.ok(claim);
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Claim> getClaim(@PathVariable Long id) {
        Optional<Claim> claim = claimService.getClaimById(id);
        return claim.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Claim>> getUserClaims(@PathVariable Long userId) {
        List<Claim> claims = claimService.getUserClaims(userId);
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Claim>> getClaimsByStatus(@PathVariable ClaimStatus status) {
        List<Claim> claims = claimService.getClaimsByStatus(status);
        return ResponseEntity.ok(claims);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Claim> activateClaim(@PathVariable Long id) {
        Claim claim = claimService.activateClaim(id);
        if (claim != null) {
            return ResponseEntity.ok(claim);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<Claim> submitClaim(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String submissionData = body.get("submissionData");
        Claim claim = claimService.submitClaim(id, submissionData);
        if (claim != null) {
            return ResponseEntity.ok(claim);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Claim> approveClaim(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String adminNotes = body.get("adminNotes");
        Claim claim = claimService.approveClaim(id, adminNotes);
        if (claim != null) {
            return ResponseEntity.ok(claim);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Claim> rejectClaim(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String rejectionReason = body.get("rejectionReason");
        Claim claim = claimService.rejectClaim(id, rejectionReason);
        if (claim != null) {
            return ResponseEntity.ok(claim);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<ClaimAudit>> getClaimHistory(@PathVariable Long id) {
        List<ClaimAudit> history = claimService.getClaimHistory(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/stats/pending")
    public ResponseEntity<Long> getPendingClaimsCount() {
        return ResponseEntity.ok(claimService.getPendingClaimCount());
    }

    @GetMapping("/stats/approved")
    public ResponseEntity<Long> getApprovedClaimsCount() {
        return ResponseEntity.ok(claimService.getApprovedClaimCount());
    }

    @GetMapping("/stats/rejected")
    public ResponseEntity<Long> getRejectedClaimsCount() {
        return ResponseEntity.ok(claimService.getRejectedClaimCount());
    }
}
