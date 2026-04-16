package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.dto.B2CSummaryResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerB2CSlaResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerDeliverySlaResponseDTO;
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

    public B2CSummaryResponseDTO calculateSummary(LocalDate dateFrom, LocalDate dateTo, String managerId, String qualification) {
        int sla1Threshold = slaConfig.getReactionMinutes();
        int sla2Threshold = slaConfig.getToAssemblyHours() * 60;
        int sla3Threshold = slaConfig.getAssemblyToDeliveryDays() * 24 * 60;
        int b2cThreshold = slaConfig.getB2cTotalDays() * 24 * 60;

        LocalDateTime start = dateFrom.atStartOfDay();
        LocalDateTime end = dateTo.plusDays(1).atStartOfDay();

        B2CSummaryResponseDTO.B2CSummaryMetrics metrics = b2cMetricsRepository.findB2CSummary(
                start,
                end,
                managerId,
                qualification,
                sla1Threshold,
                sla2Threshold,
                sla3Threshold,
                b2cThreshold);

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
        int sla1Threshold = slaConfig.getReactionMinutes();
        int sla2Threshold = slaConfig.getToAssemblyHours() * 60;
        int sla3Threshold = slaConfig.getAssemblyToDeliveryDays() * 24 * 60;
        int b2cThreshold = slaConfig.getB2cTotalDays() * 24 * 60;

        LocalDateTime start = dateFrom.atStartOfDay();
        LocalDateTime end = dateTo.plusDays(1).atStartOfDay();

        List<ManagerB2CSlaResponseDTO.ManagerB2CData> data = b2cMetricsRepository.findB2CByManager(
                start,
                end,
                managerId,
                qualification,
                sla1Threshold,
                sla2Threshold,
                sla3Threshold,
                b2cThreshold);

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