package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.dto.*;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerDeliverySlaResponseDTO.Period;
import com.ck.hackaton.artreid_3.artreid3.repository.SlaMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlaService {
    private final SlaMetricsRepository slaMetricsRepository;
    private final SlaConfig slaConfig;

    public FullSummaryResponseDTO getSlaFull(LocalDate dateFrom, LocalDate dateTo) {
        int fullThreshold = slaConfig.getFullCycleDays();
        LocalDateTime start = toStartOfDay(dateFrom);
        LocalDateTime end = toEndOfDay(dateTo);

        FullSummaryResponseDTO.FullSummaryMetrics metrics = slaMetricsRepository.findFullSummary(
                start,
                end,
                fullThreshold);

        return FullSummaryResponseDTO.builder()
                .pipeline("full")
                .period(Period.builder()
                        .from(dateFrom.toString())
                        .to(dateTo.toString())
                        .build())
                .metrics(metrics)
                .build();
    }

    private LocalDateTime toStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay();
    }
}
