package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.dto.DeliverySummaryResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerDeliverySlaResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.SlaDeliveryRequestDTO;
import com.ck.hackaton.artreid_3.artreid3.service.DeliveryMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/sla/delivery")
@RequiredArgsConstructor
public class DeliveryMetricsController {

    private final DeliveryMetricsService slaMetricsService;

    @GetMapping("/by-manager")
    public ResponseEntity<ManagerDeliverySlaResponseDTO> getDeliverySlaByManager(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) String managerId,
            @RequestParam(required = false) String qualification,
            @RequestParam(required = false) String deliveryService) {

        SlaDeliveryRequestDTO request = SlaDeliveryRequestDTO.builder()
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .deliveryManagerId(managerId)
                .leadQualification(qualification)
                .deliveryService(deliveryService)
                .build();

        return ResponseEntity.ok(slaMetricsService.getDeliverySlaByManager(request));
    }

    @GetMapping("/summary")
    public ResponseEntity<DeliverySummaryResponseDTO> getDeliverySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) String managerId,
            @RequestParam(required = false) String qualification,
            @RequestParam(required = false) String deliveryService) {

        SlaDeliveryRequestDTO request = SlaDeliveryRequestDTO.builder()
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .deliveryManagerId(managerId)
                .leadQualification(qualification)
                .deliveryService(deliveryService)
                .build();

        return ResponseEntity.ok(slaMetricsService.getDeliverySummary(request));
    }
}