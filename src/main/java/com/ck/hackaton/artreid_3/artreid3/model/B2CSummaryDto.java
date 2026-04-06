package com.ck.hackaton.artreid_3.artreid3.model;

import lombok.Value;

@Value
public class B2CSummaryDto {
    double averageFirstResponseMinutes;
    double percentile90FirstResponseMinutes;
    long totalLeads;
    long withinSlaCount;
    long violatedSlaCount;
}