package com.ck.hackaton.artreid_3.artreid3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record DeliverySummaryResponseDTO(
                ManagerDeliverySlaResponseDTO.Period period,
                String pipeline,
                DeliverySummaryMetrics metrics) {
        @Builder
        public record DeliverySummaryMetrics(
                        @JsonProperty("sla4_to_pvz") ManagerDeliverySlaResponseDTO.MetricDetails sla4ToPvz,
                        @JsonProperty("sla5_at_pvz") ManagerDeliverySlaResponseDTO.MetricDetails sla5AtPvz,
                        @JsonProperty("delivery_total") ManagerDeliverySlaResponseDTO.MetricDetails deliveryTotal) {
        }
}
