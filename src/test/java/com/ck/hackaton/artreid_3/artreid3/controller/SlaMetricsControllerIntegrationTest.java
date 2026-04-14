package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.model.ManagerDeliverySlaResponse;
import com.ck.hackaton.artreid_3.artreid3.model.DeliverySummaryResponse;
import com.ck.hackaton.artreid_3.artreid3.model.DeliverySummaryResponse.DeliverySummaryMetrics;
import com.ck.hackaton.artreid_3.artreid3.model.ManagerDeliverySlaResponse.ManagerDeliveryData;
import com.ck.hackaton.artreid_3.artreid3.model.ManagerDeliverySlaResponse.DeliveryMetrics;
import com.ck.hackaton.artreid_3.artreid3.model.ManagerDeliverySlaResponse.MetricDetails;
import com.ck.hackaton.artreid_3.artreid3.model.ManagerDeliverySlaResponse.BreachDistribution;
import com.ck.hackaton.artreid_3.artreid3.model.ManagerDeliverySlaResponse.Period;
import com.ck.hackaton.artreid_3.artreid3.service.SlaMetricsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SlaMetricsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SlaMetricsService slaMetricsService;

    @MockitoBean
    private BuildProperties buildProperties;

    @Test
    void getDeliverySlaByManager_withAllParams_returnsMetrics() throws Exception {
        ManagerDeliveryData metric = ManagerDeliveryData.builder()
                .managerId("mgr_123")
                .metrics(DeliveryMetrics.builder()
                        .sla4ToPvz(MetricDetails.builder()
                                .thresholdMinutes(7200)
                                .totalOrders(42)
                                .metCount(38)
                                .metPercent(90.48)
                                .breachCount(4)
                                .breachPercent(9.52)
                                .avgMinutes(185.50)
                                .medianMinutes(172.00)
                                .p90Minutes(310.20)
                                .breachDistribution(BreachDistribution.builder()
                                        .upTo1Day(2).oneTo3Days(1).over3Days(1).build())
                                .build())
                        .build())
                .build();
                
        ManagerDeliverySlaResponse response = ManagerDeliverySlaResponse.builder()
               .pipeline("delivery")
               .period(Period.builder().from("2026-03-01").to("2026-03-31").build())
               .data(List.of(metric))
               .build();
               
        when(slaMetricsService.getDeliverySlaByManager(any())).thenReturn(response);

        mockMvc.perform(get("/api/sla/delivery/by-manager")
                        .param("dateFrom", "2026-03-01T00:00:00")
                        .param("dateTo", "2026-03-31T23:59:59")
                        .param("managerId", "mgr_123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.pipeline").value("delivery"))
                .andExpect(jsonPath("$.data[0].manager_id").value("mgr_123"))
                .andExpect(jsonPath("$.data[0].metrics.sla4_to_pvz.total_orders").value(42));
    }

    @Test
    void getDeliverySummary_withAllParams_returnsSummary() throws Exception {
        DeliverySummaryMetrics metrics = DeliverySummaryMetrics.builder()
                .sla4ToPvz(MetricDetails.builder()
                        .thresholdMinutes(7200)
                        .totalOrders(100)
                        .metCount(90)
                        .metPercent(90.0)
                        .breachCount(10)
                        .breachPercent(10.0)
                        .avgMinutes(180.0)
                        .medianMinutes(170.0)
                        .p90Minutes(300.0)
                        .breachDistribution(BreachDistribution.builder()
                                .upTo1Day(5).oneTo3Days(3).over3Days(2).build())
                        .build())
                .build();
                
        DeliverySummaryResponse response = DeliverySummaryResponse.builder()
               .pipeline("delivery")
               .period(Period.builder().from("2026-03-01").to("2026-03-31").build())
               .metrics(metrics)
               .build();
               
        when(slaMetricsService.getDeliverySummary(any())).thenReturn(response);

        mockMvc.perform(get("/api/sla/delivery/summary")
                        .param("dateFrom", "2026-03-01T00:00:00")
                        .param("dateTo", "2026-03-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.pipeline").value("delivery"))
                .andExpect(jsonPath("$.metrics.sla4_to_pvz.threshold_minutes").value(7200))
                .andExpect(jsonPath("$.metrics.sla4_to_pvz.total_orders").value(100));
    }

    @Test
    void getDeliverySlaByManager_withInvalidDateFormat_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/sla/delivery/by-manager")
                        .param("dateFrom", "invalid-date"))
                .andExpect(status().isBadRequest());
    }
}
