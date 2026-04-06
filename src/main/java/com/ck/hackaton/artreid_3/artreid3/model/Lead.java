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
    @Column(name = "lead_id", nullable = false)
    private String leadId; // ← PK из CSV, например "LEAD_0172"

    @Column(name = "lead_responsible_user_id")
    private String leadResponsibleUserId;

    @Column(name = "lead_created_at")
    private Long leadCreatedAt; // Unix timestamp (секунды)

    @Column(name = "sale_ts")
    private Long saleTs; // Unix timestamp (секунды)

    // Остальные поля можно добавить позже при необходимости
}