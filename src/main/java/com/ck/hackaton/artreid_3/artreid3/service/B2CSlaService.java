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
                slaConfig.getReactionMinutes(),
                slaConfig.getToAssemblyHours() * 60,
                slaConfig.getAssemblyToDeliveryDays() * 24 * 60,
                slaConfig.getB2cTotalDays() * 24 * 60
        );
    }

    private LocalDateTime toStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay();
    }

    public B2CSummaryResponseDTO calculateSummary(LocalDate dateFrom, LocalDate dateTo, String managerId, String qualification) {
        B2CThresholds t = resolveThresholds();
        LocalDateTime start = toStartOfDay(dateFrom);
        LocalDateTime end = toEndOfDay(dateTo);

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
                        .from(dateFrom.toString())
                        .to(dateTo.toString())
                        .build())
                .metrics(metrics)
                .build();
    }

    public ManagerB2CSlaResponseDTO getB2CSlaByManager(LocalDate dateFrom, LocalDate dateTo, String managerId, String qualification) {
        B2CThresholds t = resolveThresholds();
        LocalDateTime start = toStartOfDay(dateFrom);
        LocalDateTime end = toEndOfDay(dateTo);

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
                        .from(dateFrom.toString())
                        .to(dateTo.toString())
                        .build())
                .data(data)
                .build();
    }

    public B2CSummaryResponseDTO calculateSummary(LocalDate dateFrom, LocalDate dateTo, String managerId) {
        return calculateSummary(dateFrom, dateTo, managerId, null);
    }
}