package com.ck.hackaton.artreid_3.artreid3.dto;

import lombok.Builder;
import lombok.Value;
import java.util.Map;

@Value
@Builder
public class B2CSummaryDto {
    // Основные метрики
    long totalLeads;
    long withinSlaCount;
    long violatedSlaCount;
    double withinSlaPercent;
    double violatedSlaPercent;

    // Статистика времени реакции
    double averageFirstResponseMinutes;
    double medianFirstResponseMinutes;
    double percentile90FirstResponseMinutes;
    double minMinutes;
    double maxMinutes;

    // Распределение нарушений по интервалам
    Map<String, Long> breachDistribution;

    // Детализация по менеджерам (если нужно)
    Map<String, ManagerStats> managerStats;

    @Value
    @Builder
    public static class ManagerStats {
        long totalLeads;
        long withinSlaCount;
        double averageMinutes;
        double percentile90Minutes;
    }
}