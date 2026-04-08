package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.model.ManagerDeliverySlaMetrics;
import com.ck.hackaton.artreid_3.artreid3.model.SlaDeliveryRequest;
import com.ck.hackaton.artreid_3.artreid3.service.SlaMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sla/delivery")
@RequiredArgsConstructor
public class SlaMetricsController {

    private final SlaMetricsService slaMetricsService;

    @GetMapping("/by-manager")
    public ResponseEntity<List<ManagerDeliverySlaMetrics>> getDeliverySlaByManager(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) String managerId) {

        SlaDeliveryRequest request = SlaDeliveryRequest.builder()
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .managerId(managerId)
                .build();

        return ResponseEntity.ok(slaMetricsService.getDeliverySlaByManager(request));
    }
}