package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.dto.B2CSummaryResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.FullSummaryResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerB2CSlaResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.DeliverySummaryResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerDeliverySlaResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.SlaDeliveryRequestDTO;
import com.ck.hackaton.artreid_3.artreid3.service.B2CSlaService;
import com.ck.hackaton.artreid_3.artreid3.service.DeliveryMetricsService;
import com.ck.hackaton.artreid_3.artreid3.service.SlaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@RestController
@RequestMapping("/api/sla")
@RequiredArgsConstructor
public class SlaAnalyticsController {

    private final B2CSlaService b2CSlaService;
    private final SlaService slaService;
    private final DeliveryMetricsService deliveryMetricsService;

    @GetMapping("/b2c/summary")
    public B2CSummaryResponseDTO getB2CSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String managerId,
            @RequestParam(required = false) String qualification) {
        return b2CSlaService.calculateSummary(dateFrom, dateTo, managerId, qualification);
    }

    @GetMapping("/b2c/by-manager")
    public ManagerB2CSlaResponseDTO getB2CSlaByManager(
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String managerId,
            @RequestParam(required = false) String qualification) {
        return b2CSlaService.getB2CSlaByManager(dateFrom, dateTo, managerId, qualification);
    }

    @GetMapping("/full/summary")
    public FullSummaryResponseDTO getFullSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateTo) {
        return slaService.getSlaFull(dateFrom, dateTo);
    }

    @GetMapping("/delivery/by-manager")
    public ManagerDeliverySlaResponseDTO getDeliverySlaByManager(
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

        return deliveryMetricsService.getDeliverySlaByManager(request);
    }

    @GetMapping("/delivery/summary")
    public DeliverySummaryResponseDTO getDeliverySummary(
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

        return deliveryMetricsService.getDeliverySummary(request);
    }
}