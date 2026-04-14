package com.ck.hackaton.artreid_3.artreid3.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record DeliverySummaryResponse(
        ManagerDeliverySlaResponse.Period period,
        String pipeline,
        DeliverySummaryMetrics metrics
) {
    @Builder
    public record DeliverySummaryMetrics(
            @JsonProperty("sla4_to_pvz") ManagerDeliverySlaResponse.MetricDetails sla4ToPvz,
            @JsonProperty("sla5_at_pvz") ManagerDeliverySlaResponse.MetricDetails sla5AtPvz,
            @JsonProperty("delivery_total") ManagerDeliverySlaResponse.MetricDetails deliveryTotal
    ) {}
}
