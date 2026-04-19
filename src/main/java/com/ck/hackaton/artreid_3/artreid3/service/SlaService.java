package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.dto.*;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerDeliverySlaResponseDTO.Period;
import com.ck.hackaton.artreid_3.artreid3.repository.SlaMetricsRepository;
import com.ck.hackaton.artreid_3.artreid3.util.DateResolutionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlaService {
    private final SlaMetricsRepository slaMetricsRepository;
    private final SlaConfig slaConfig;

    public FullSummaryResponseDTO getSlaFull(LocalDate dateFrom, LocalDate dateTo) {
        int fullThreshold = slaConfig.getFullCycleDays();
        LocalDateTime start = DateResolutionUtil.toStartOfDay(dateFrom);
        LocalDateTime end = DateResolutionUtil.toEndOfDay(dateTo);

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
}
