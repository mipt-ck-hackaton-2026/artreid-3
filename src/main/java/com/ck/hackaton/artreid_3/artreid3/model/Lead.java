package com.ck.hackaton.artreid_3.artreid3.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "leads")
@Getter
@Setter
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lead_id")
    private Long leadId;

    @Column(name = "external_lead_id", length = 50)
    private String externalLeadId;

    @Column(name = "manager_id", length = 50)
    private String managerId;

    @Column(name = "pipeline_id")
    private Integer pipelineId;

    @Column(name = "delivery_service", length = 100)
    private String deliveryService;

    @Column(name = "city", length = 100)
    private String city;
}