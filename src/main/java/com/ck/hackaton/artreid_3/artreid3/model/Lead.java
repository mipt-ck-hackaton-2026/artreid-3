package com.ck.hackaton.artreid_3.artreid3.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lead_id")
    private Long leadId;

    @Column(name = "external_lead_id", nullable = false, unique = true)
    private String externalLeadId;

    @Column(name = "manager_id")
    private String managerId;

    @Column(name = "pipeline_id")
    private Integer pipelineId;

    @Column(name = "delivery_service")
    private String deliveryService;

    @Column(name = "city")
    private String city;
}
