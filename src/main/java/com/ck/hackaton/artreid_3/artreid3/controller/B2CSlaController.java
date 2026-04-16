package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.dto.B2CSummaryResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerB2CSlaResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.service.B2CSlaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@RestController
@RequestMapping("/api/sla/b2c")
public class B2CSlaController {

    private final B2CSlaService b2CSlaService;

    public B2CSlaController(B2CSlaService b2CSlaService) {
        this.b2CSlaService = b2CSlaService;
    }

    @GetMapping("/summary")
    public B2CSummaryResponseDTO getB2CSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String managerId,
            @RequestParam(required = false) String qualification) {
        LocalDate actualDateFrom = dateFrom != null ? dateFrom : LocalDate.now().minusDays(30);
        LocalDate actualDateTo = dateTo != null ? dateTo : LocalDate.now();

        if (actualDateFrom.isAfter(actualDateTo)) {
            throw new IllegalArgumentException("dateFrom must be <= dateTo");
        }
        return b2CSlaService.calculateSummary(actualDateFrom, actualDateTo, managerId, qualification);
    }

    @GetMapping("/by-manager")
    public ResponseEntity<ManagerB2CSlaResponseDTO> getB2CSlaByManager(
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String managerId,
            @RequestParam(required = false) String qualification) {
        LocalDate actualDateFrom = dateFrom != null ? dateFrom : LocalDate.now().minusDays(30);
        LocalDate actualDateTo = dateTo != null ? dateTo : LocalDate.now();

        if (actualDateFrom.isAfter(actualDateTo)) {
            throw new IllegalArgumentException("dateFrom must be <= dateTo");
        }
        return ResponseEntity.ok(b2CSlaService.getB2CSlaByManager(actualDateFrom, actualDateTo, managerId, qualification));
    }
}