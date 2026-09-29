package com.anomaly.transactionanomalybackend.controller;

import com.anomaly.transactionanomalybackend.dto.AnalyticsStats;
import com.anomaly.transactionanomalybackend.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public ResponseEntity<AnalyticsStats> getAnalytics() {
        return ResponseEntity.ok(
                analyticsService.getAnalytics()
        );
    }
}