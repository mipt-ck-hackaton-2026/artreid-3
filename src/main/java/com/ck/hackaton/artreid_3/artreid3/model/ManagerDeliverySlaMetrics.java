package com.ck.hackaton.artreid_3.artreid3.model;

import java.math.BigDecimal;

public record ManagerDeliverySlaMetrics(
        String managerId,
        Long totalCount,
        BigDecimal avgMinutes,
        BigDecimal medianMinutes,
        BigDecimal p90Minutes,
        Long withinSlaCount,
        BigDecimal withinSlaPercent
) { }