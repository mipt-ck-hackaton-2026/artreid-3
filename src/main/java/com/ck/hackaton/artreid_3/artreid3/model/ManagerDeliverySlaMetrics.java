package com.ck.hackaton.artreid_3.artreid3.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerDeliverySlaMetrics {
    private String managerId;
    private Long totalCount;
    private BigDecimal avgMinutes;
    private BigDecimal medianMinutes;
    private BigDecimal p90Minutes;
    private Long withinSlaCount;
    private BigDecimal withinSlaPercent;
}