package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.dto.B2CSummaryResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.FullSummaryResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerB2CSlaResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.service.B2CSlaService;
import com.ck.hackaton.artreid_3.artreid3.service.SlaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.ck.hackaton.artreid_3.artreid3.util.DateResolutionUtil;
import com.ck.hackaton.artreid_3.artreid3.util.DateValidationUtil;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@RestController
@RequestMapping("/api/sla")
@RequiredArgsConstructor
public class SlaAnalyticsController {

    private final B2CSlaService b2CSlaService;
    private final SlaService SlaService;

    @GetMapping("/b2c/summary")
    public B2CSummaryResponseDTO getB2CSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String managerId,
            @RequestParam(required = false) String qualification) {
        DateValidationUtil.validateDateRange(dateFrom, dateTo);
        return b2CSlaService.calculateSummary(dateFrom, dateTo, managerId, qualification);
    }

    @GetMapping("/b2c/by-manager")
    public ManagerB2CSlaResponseDTO getB2CSlaByManager(
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String managerId,
            @RequestParam(required = false) String qualification) {
        DateValidationUtil.validateDateRange(dateFrom, dateTo);
        return b2CSlaService.getB2CSlaByManager(dateFrom, dateTo, managerId, qualification);
    }
    @GetMapping("/full/summary")
    public FullSummaryResponseDTO getB2CSlaByManager(
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateTo) {
        DateValidationUtil.validateDateRange(dateFrom, dateTo);
        LocalDate[] range = DateResolutionUtil.resolveDateRange(dateFrom, dateTo);
        return SlaService.getSlaFull(range[0], range[1]);
    }
}