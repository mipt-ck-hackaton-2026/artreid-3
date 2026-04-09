package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.dto.DeliverySummaryResponse;
import com.ck.hackaton.artreid_3.artreid3.service.DeliverySlaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/sla/delivery")
@RequiredArgsConstructor
public class DeliverySlaController {

    private final DeliverySlaService service;

    @GetMapping("/summary")
    public DeliverySummaryResponse getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String managerId,
            @RequestParam(required = false) String deliveryService
    ) {
        if (dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom must be before or equal dateTo");
        }
        return service.getDeliverySummary(dateFrom, dateTo, managerId, deliveryService);
    }
}