package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.model.ManagerDeliverySlaMetrics;
import com.ck.hackaton.artreid_3.artreid3.service.SlaMetricsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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
        ManagerDeliverySlaMetrics metric = new ManagerDeliverySlaMetrics(
                "mgr_123",
                42L,
                new BigDecimal("185.50"),
                new BigDecimal("172.00"),
                new BigDecimal("310.20"),
                38L,
                new BigDecimal("90.48")
        );
        when(slaMetricsService.getDeliverySlaByManager(any())).thenReturn(List.of(metric));

        mockMvc.perform(get("/api/sla/delivery/by-manager")
                        .param("dateFrom", "2026-03-01T00:00:00")
                        .param("dateTo", "2026-03-31T23:59:59")
                        .param("managerId", "mgr_123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$[0].managerId").value("mgr_123"))
                .andExpect(jsonPath("$[0].totalCount").value(42))
                .andExpect(jsonPath("$[0].avgMinutes").value(185.50))
                .andExpect(jsonPath("$[0].medianMinutes").value(172.00))
                .andExpect(jsonPath("$[0].p90Minutes").value(310.20))
                .andExpect(jsonPath("$[0].withinSlaCount").value(38))
                .andExpect(jsonPath("$[0].withinSlaPercent").value(90.48));
    }

    @Test
    void getDeliverySlaByManager_withoutManagerId_aggregatesAllManagers() throws Exception {
        ManagerDeliverySlaMetrics metric1 = new ManagerDeliverySlaMetrics(
                "mgr_1", 10L, new BigDecimal("100"), new BigDecimal("90"),
                new BigDecimal("150"), 9L, new BigDecimal("90.00")
        );
        ManagerDeliverySlaMetrics metric2 = new ManagerDeliverySlaMetrics(
                "mgr_2", 20L, new BigDecimal("200"), new BigDecimal("180"),
                new BigDecimal("400"), 15L, new BigDecimal("75.00")
        );
        when(slaMetricsService.getDeliverySlaByManager(any()))
                .thenReturn(List.of(metric1, metric2));

        mockMvc.perform(get("/api/sla/delivery/by-manager")
                        .param("dateFrom", "2026-01-01T00:00:00")
                        .param("dateTo", "2026-04-01T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].managerId").value("mgr_1"))
                .andExpect(jsonPath("$[1].managerId").value("mgr_2"));
    }

    @Test
    void getDeliverySlaByManager_withoutDates_usesDefaults() throws Exception {
        when(slaMetricsService.getDeliverySlaByManager(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/sla/delivery/by-manager"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getDeliverySlaByManager_withInvalidDateFormat_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/sla/delivery/by-manager")
                        .param("dateFrom", "invalid-date"))
                .andExpect(status().isBadRequest());
    }
}