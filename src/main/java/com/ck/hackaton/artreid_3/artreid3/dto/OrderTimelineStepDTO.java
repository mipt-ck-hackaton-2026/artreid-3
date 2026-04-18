package com.ck.hackaton.artreid_3.artreid3.dto;

import com.ck.hackaton.artreid_3.artreid3.model.StageName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderTimelineStepDTO {
    private StageName stage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMinutes;
    private Double durationDays;
    private boolean slaViolated;
    private String slaThreshold;
}