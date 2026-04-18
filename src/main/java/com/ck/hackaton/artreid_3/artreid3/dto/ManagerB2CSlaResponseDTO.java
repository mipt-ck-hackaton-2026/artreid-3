package com.ck.hackaton.artreid_3.artreid3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record ManagerB2CSlaResponseDTO(
        ManagerDeliverySlaResponseDTO.Period period,
        String pipeline,
        List<ManagerB2CData> data) {
    @Builder
    public record ManagerB2CData(
            @JsonProperty("manager_id") String managerId,
            B2CMetrics metrics) {
    }

    @Builder
    public record B2CMetrics(
            @JsonProperty("sla1_reaction") B2CSummaryResponseDTO.B2CMetricDetails sla1Reaction,
            @JsonProperty("sla2_to_assembly") B2CSummaryResponseDTO.B2CMetricDetails sla2ToAssembly,
            @JsonProperty("sla3_to_delivery") B2CSummaryResponseDTO.B2CMetricDetails sla3ToDelivery,
            @JsonProperty("b2c_total") B2CSummaryResponseDTO.B2CMetricDetails b2cTotal) {
    }
}
