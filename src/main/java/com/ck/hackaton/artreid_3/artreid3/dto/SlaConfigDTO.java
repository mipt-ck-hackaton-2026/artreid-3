package com.ck.hackaton.artreid_3.artreid3.dto;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for exposing SLA configuration via API.
 * Prevents serialization of Spring internal proxy fields (e.g. $$beanFactory).
 */
public record SlaConfigDTO(
        B2cDTO b2c,
        DeliveryDTO delivery,
        @JsonProperty("full_cycle_days") int fullCycleDays,
        @JsonProperty("breach_buckets") BreachBucketsDTO breachBuckets) {

    public record B2cDTO(
            @JsonProperty("reaction_minutes") int reactionMinutes,
            @JsonProperty("to_assembly_hours") int toAssemblyHours,
            @JsonProperty("assembly_to_delivery_days") int assemblyToDeliveryDays,
            @JsonProperty("total_days") int totalDays) {
    }

    public record DeliveryDTO(
            @JsonProperty("to_pvz_days") int toPvzDays,
            @JsonProperty("pvz_storage_days") int pvzStorageDays,
            @JsonProperty("total_days") int totalDays) {
    }

    public record BreachBucketsDTO(
            @JsonProperty("short_minutes") int[] shortMinutes,
            int[] days) {
    }

    public static SlaConfigDTO from(SlaConfig config) {
        return new SlaConfigDTO(
                new B2cDTO(
                        config.getB2c().getReactionMinutes(),
                        config.getB2c().getToAssemblyHours(),
                        config.getB2c().getAssemblyToDeliveryDays(),
                        config.getB2c().getTotalDays()),
                new DeliveryDTO(
                        config.getDelivery().getToPvzDays(),
                        config.getDelivery().getPvzStorageDays(),
                        config.getDelivery().getTotalDays()),
                config.getFullCycleDays(),
                new BreachBucketsDTO(
                        config.getBreachBuckets().getShortMinutes(),
                        config.getBreachBuckets().getDays()));
    }
}
