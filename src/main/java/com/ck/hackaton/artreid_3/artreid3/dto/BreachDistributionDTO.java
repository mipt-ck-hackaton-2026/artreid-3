package com.ck.hackaton.artreid_3.artreid3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreachDistributionDTO {
    
    @JsonProperty("metadata")
    private Metadata metadata;

    @JsonProperty("items")
    private List<Item> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        @JsonProperty("unit")
        private String unit;
        
        @JsonProperty("total_count")
        private long totalCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        @JsonProperty("sort_order")
        private int sortOrder;
        
        @JsonProperty("min_bound")
        private Integer minBound;
        
        @JsonProperty("max_bound")
        private Integer maxBound;
        
        @JsonProperty("count")
        private long count;
        
        @JsonProperty("ratio")
        private double ratio;
    }
}
