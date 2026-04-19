package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.dto.B2CSummaryResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerB2CSlaResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerDeliverySlaResponseDTO.Period;
import com.ck.hackaton.artreid_3.artreid3.repository.B2CMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.ck.hackaton.artreid_3.artreid3.util.DateResolutionUtil;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class B2CSlaService {

    private final B2CMetricsRepository b2cMetricsRepository;
    private final SlaConfig slaConfig;

    private record B2CThresholds(int sla1, int sla2, int sla3, int b2c) {}

    private B2CThresholds resolveThresholds() {
        return new B2CThresholds(
                slaConfig.getB2c().getReactionMinutes(),
                slaConfig.getB2c().getToAssemblyHours() * 60,
                slaConfig.getB2c().getAssemblyToDeliveryDays() * 24 * 60,
                slaConfig.getB2c().getTotalDays() * 24 * 60
        );
    }

    public B2CSummaryResponseDTO calculateSummary(LocalDate dateFrom, LocalDate dateTo, String managerId, String qualification) {
        B2CThresholds t = resolveThresholds();
        LocalDate[] range = DateResolutionUtil.resolveDateRange(dateFrom, dateTo);
        LocalDateTime start = DateResolutionUtil.toStartOfDay(range[0]);
        LocalDateTime end = DateResolutionUtil.toEndOfDay(range[1]);

        B2CSummaryResponseDTO.B2CSummaryMetrics metrics = b2cMetricsRepository.findB2CSummary(
                start,
                end,
                managerId,
                qualification,
                t.sla1(),
                t.sla2(),
                t.sla3(),
                t.b2c());

        return B2CSummaryResponseDTO.builder()
                .pipeline("b2c")
                .period(Period.builder()
                        .from(range[0].toString())
                        .to(range[1].toString())
                        .build())
                .metrics(metrics)
                .build();
    }

    public ManagerB2CSlaResponseDTO getB2CSlaByManager(LocalDate dateFrom, LocalDate dateTo, String managerId, String qualification) {
        B2CThresholds t = resolveThresholds();
        LocalDate[] range = DateResolutionUtil.resolveDateRange(dateFrom, dateTo);
        LocalDateTime start = DateResolutionUtil.toStartOfDay(range[0]);
        LocalDateTime end = DateResolutionUtil.toEndOfDay(range[1]);

        List<ManagerB2CSlaResponseDTO.ManagerB2CData> data = b2cMetricsRepository.findB2CByManager(
                start,
                end,
                managerId,
                qualification,
                t.sla1(),
                t.sla2(),
                t.sla3(),
                t.b2c());

        return ManagerB2CSlaResponseDTO.builder()
                .pipeline("b2c")
                .period(Period.builder()
                        .from(range[0].toString())
                        .to(range[1].toString())
                        .build())
                .data(data)
                .build();
    }

    public B2CSummaryResponseDTO calculateSummary(LocalDate dateFrom, LocalDate dateTo, String managerId) {
        return calculateSummary(dateFrom, dateTo, managerId, null);
    }
}