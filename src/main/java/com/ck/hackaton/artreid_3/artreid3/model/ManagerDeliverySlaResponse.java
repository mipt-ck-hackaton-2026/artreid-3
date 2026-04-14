package com.ck.hackaton.artreid_3.artreid3.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record ManagerDeliverySlaResponse(
        Period period,
        String pipeline,
        List<ManagerDeliveryData> data
) {
    @Builder
    public record Period(
            String from,
            String to
    ) {}

    @Builder
    public record ManagerDeliveryData(
            @JsonProperty("manager_id") String managerId,
            DeliveryMetrics metrics
    ) {}

    @Builder
    public record DeliveryMetrics(
            @JsonProperty("sla4_to_pvz") MetricDetails sla4ToPvz,
            @JsonProperty("sla5_at_pvz") MetricDetails sla5AtPvz,
            @JsonProperty("delivery_total") MetricDetails deliveryTotal
    ) {}

    @Builder
    public record MetricDetails(
            @JsonProperty("threshold_minutes") int thresholdMinutes,
            @JsonProperty("total_orders") long totalOrders,
            @JsonProperty("met_count") long metCount,
            @JsonProperty("met_percent") double metPercent,
            @JsonProperty("breach_count") long breachCount,
            @JsonProperty("breach_percent") double breachPercent,
            @JsonProperty("avg_minutes") double avgMinutes,
            @JsonProperty("median_minutes") double medianMinutes,
            @JsonProperty("p90_minutes") double p90Minutes,
            @JsonProperty("breach_distribution") BreachDistribution breachDistribution
    ) {}

    @Builder
    public record BreachDistribution(
            @JsonProperty("up_to_1day") long upTo1Day,
            @JsonProperty("1_to_3days") long oneTo3Days,
            @JsonProperty("over_3days") long over3Days
    ) {}
}
