package com.ck.hackaton.artreid_3.artreid3.dto;

import java.time.LocalDate;

public record DeliverySummaryResponse(
    LocalDate dateFrom,
    LocalDate dateTo,
    double avgHandedToDeliveryDays,
    double avgPvzToReceivedDays,
    int totalOrders,
    double slaComplianceRate
) {}