package com.viva.benefits_engine.controller;

import com.viva.benefits_engine.models.Metrics;
import com.viva.benefits_engine.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin(origins = "*")
public class MetricsController {

    @Autowired
    private MetricsService metricsService;

    @PostMapping("/record")
    public ResponseEntity<Metrics> recordMetrics(
            @RequestParam BigDecimal detectionAccuracy,
            @RequestParam BigDecimal prefillQuality) {
        Metrics metrics = metricsService.recordMetrics(detectionAccuracy, prefillQuality);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatestMetrics() {
        Metrics metrics = metricsService.getLatestMetrics();

        if (metrics == null) {
            return ResponseEntity.ok(getEmptyMetrics());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("detectionAccuracy", metrics.getDetectionAccuracy());
        response.put("prefillQuality", metrics.getPrefillQuality());
        response.put("unclaimedBenefitCount", metrics.getUnclaimedBenefitCount());
        response.put("totalEligibleCount", metrics.getTotalEligibleCount());
        response.put("totalClaimsActivated", metrics.getTotalClaimsActivated());
        response.put("totalClaimsApproved", metrics.getTotalClaimsApproved());
        response.put("avgClaimProcessingTime", metrics.getAvgClaimProcessingTime());
        response.put("recordedAt", metrics.getRecordedAt());

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> getEmptyMetrics() {
        Map<String, Object> empty = new HashMap<>();
        empty.put("detectionAccuracy", BigDecimal.ZERO);
        empty.put("prefillQuality", BigDecimal.ZERO);
        empty.put("unclaimedBenefitCount", 0L);
        empty.put("totalEligibleCount", 0L);
        empty.put("totalClaimsActivated", 0L);
        empty.put("totalClaimsApproved", 0L);
        empty.put("avgClaimProcessingTime", BigDecimal.ZERO);
        empty.put("recordedAt", null);
        return empty;
    }
}
