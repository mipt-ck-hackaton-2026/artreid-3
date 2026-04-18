package com.ck.hackaton.artreid_3.artreid3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record B2CSummaryResponseDTO(
        ManagerDeliverySlaResponseDTO.Period period,
        String pipeline,
        B2CSummaryMetrics metrics) {

    @Builder
    public record B2CSummaryMetrics(
            @JsonProperty("sla1_reaction") B2CMetricDetails sla1Reaction,
            @JsonProperty("sla2_to_assembly") B2CMetricDetails sla2ToAssembly,
            @JsonProperty("sla3_to_delivery") B2CMetricDetails sla3ToDelivery,
            @JsonProperty("b2c_total") B2CMetricDetails b2cTotal) {
    }

    @Builder
    public record B2CMetricDetails(
            @JsonProperty("threshold_minutes") int thresholdMinutes,
            @JsonProperty("total_orders") long totalOrders,
            @JsonProperty("met_count") long metCount,
            @JsonProperty("met_percent") double metPercent,
            @JsonProperty("breach_count") long breachCount,
            @JsonProperty("breach_percent") double breachPercent,
            @JsonProperty("avg_minutes") double avgMinutes,
            @JsonProperty("median_minutes") double medianMinutes,
            @JsonProperty("p90_minutes") double p90Minutes,
            @JsonProperty("breach_distribution") BreachDistributionDTO breachDistribution) {
    }

}
