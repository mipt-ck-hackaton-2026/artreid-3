package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.dto.B2CSummaryResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.B2CSummaryResponseDTO.B2CMetricDetails;
import com.ck.hackaton.artreid_3.artreid3.dto.B2CSummaryResponseDTO.B2CSummaryMetrics;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerDeliverySlaResponseDTO.Period;
import com.ck.hackaton.artreid_3.artreid3.service.B2CSlaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(B2CSlaController.class)
class B2CSlaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private B2CSlaService b2CSlaService;

    @MockitoBean
    private BuildProperties buildProperties;

    @Test
    void getB2CSummary_returnsSummary() throws Exception {
        B2CSummaryMetrics metrics = B2CSummaryMetrics.builder()
                .sla1Reaction(B2CMetricDetails.builder()
                        .thresholdMinutes(30)
                        .totalOrders(10)
                        .metCount(8)
                        .build())
                .build();

        B2CSummaryResponseDTO responseDTO = B2CSummaryResponseDTO.builder()
                .pipeline("b2c")
                .period(Period.builder().from("2026-03-01").to("2026-03-31").build())
                .metrics(metrics)
                .build();

        when(b2CSlaService.calculateSummary(any(LocalDate.class), any(LocalDate.class), eq("mgr1"), eq("A")))
                .thenReturn(responseDTO);

        mockMvc.perform(get("/api/sla/b2c/summary")
                .param("dateFrom", "2026-03-01")
                .param("dateTo", "2026-03-31")
                .param("managerId", "mgr1")
                .param("qualification", "A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pipeline").value("b2c"))
                .andExpect(jsonPath("$.period.from").value("2026-03-01"))
                .andExpect(jsonPath("$.period.to").value("2026-03-31"))
                .andExpect(jsonPath("$.metrics.sla1_reaction.threshold_minutes").value(30))
                .andExpect(jsonPath("$.metrics.sla1_reaction.total_orders").value(10));
    }

    @Test
    void getB2CSummary_dateFromAfterDateTo_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/sla/b2c/summary")
                .param("dateFrom", "2026-03-31")
                .param("dateTo", "2026-03-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("dateFrom must be <= dateTo"));
    }
}
