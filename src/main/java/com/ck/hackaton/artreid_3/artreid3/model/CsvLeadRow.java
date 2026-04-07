package com.ck.hackaton.artreid_3.artreid3.model;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CsvLeadRow {

    @CsvBindByName(column = "lead_id")
    private String leadId;

    @CsvBindByName(column = "sale_ts")
    private String saleTs;

    @CsvBindByName(column = "handed_to_delivery_ts")
    private String handedToDeliveryTs;

    @CsvBindByName(column = "issued_or_pvz_ts")
    private String issuedOrPvzTs;

    @CsvBindByName(column = "received_ts")
    private String receivedTs;

    @CsvBindByName(column = "rejected_ts")
    private String rejectedTs;

    @CsvBindByName(column = "returned_ts")
    private String returnedTs;

    @CsvBindByName(column = "lead_responsible_user_id")
    private String managerId;

    @CsvBindByName(column = "lead_pipeline_id")
    private Integer pipelineId;

    @CsvBindByName(column = "lead_Служба доставки")
    private String deliveryService;

    @CsvBindByName(column = "contact_Город")
    private String city;
}
