package com.ck.hackaton.artreid_3.artreid3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record FullSummaryResponseDTO(
        ManagerDeliverySlaResponseDTO.Period period,
        String pipeline,
        FullSummaryMetrics metrics
) {
    @Builder
    public record FullSummaryMetrics(
            @JsonProperty("full_total") B2CSummaryResponseDTO.B2CMetricDetails fullTotal) {
    }
}
