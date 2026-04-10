package com.ck.hackaton.artreid_3.artreid3.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "lead_events")
@Getter
@Setter
public class LeadEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lead_event_id")
    private Long leadEventId;

    @Column(name = "lead_id", nullable = false)
    private Long leadId;

    @Column(name = "stage_name", nullable = false, length = 50)
    private String stageName;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;
}