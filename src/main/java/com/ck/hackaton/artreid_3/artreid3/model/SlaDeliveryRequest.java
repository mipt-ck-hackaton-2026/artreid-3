package com.ck.hackaton.artreid_3.artreid3.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaDeliveryRequest {
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private String deliveryManagerId;
    private String leadQualification;
    private String deliveryService;
}