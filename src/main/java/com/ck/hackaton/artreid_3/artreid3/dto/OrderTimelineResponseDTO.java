package com.ck.hackaton.artreid_3.artreid3.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OrderTimelineResponseDTO {
    private PeriodDto period;
    private String pipeline;
    private List<OrderTimelineStepDTO> data;

    @Data
    @Builder
    public static class PeriodDto {
        private String from;
        private String to;
    }
}