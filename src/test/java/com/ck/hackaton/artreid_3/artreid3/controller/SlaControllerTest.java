package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.model.B2CSummaryDto;
import com.ck.hackaton.artreid_3.artreid3.service.B2CSlaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SlaController.class)
@DisplayName("Тесты SLA контроллера")
class SlaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SlaConfig slaConfig;

    @MockBean
    private B2CSlaService b2cSlaService;

    @Test
    @DisplayName("GET /api/sla/config - должен вернуть конфигурацию")
    void shouldReturnConfig() throws Exception {
        when(slaConfig.getFirstResponseNormativeMinutes()).thenReturn(30);

        mockMvc.perform(get("/api/sla/config"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstResponseNormativeMinutes").value(30));
    }

    @Test
    @DisplayName("GET /api/sla/b2c/summary - должен вернуть агрегаты")
    void shouldReturnB2CSummary() throws Exception {
        B2CSummaryDto mockResponse = B2CSummaryDto.builder()
            .totalLeads(5)
            .withinSlaCount(3)
            .violatedSlaCount(2)
            .withinSlaPercent(60.0)
            .violatedSlaPercent(40.0)
            .averageFirstResponseMinutes(33.6)
            .medianFirstResponseMinutes(25.0)
            .percentile90FirstResponseMinutes(90.0)
            .minMinutes(3.0)
            .maxMinutes(90.0)
            .breachDistribution(new HashMap<>())
            .managerStats(new HashMap<>())
            .build();

        when(b2cSlaService.calculateSummary(any(LocalDate.class), any(LocalDate.class), any(), any()))
            .thenReturn(mockResponse);

        mockMvc.perform(get("/api/sla/b2c/summary")
                .param("dateFrom", "2026-04-01")
                .param("dateTo", "2026-04-10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalLeads").value(5))
            .andExpect(jsonPath("$.withinSlaCount").value(3))
            .andExpect(jsonPath("$.violatedSlaCount").value(2))
            .andExpect(jsonPath("$.withinSlaPercent").value(60.0))
            .andExpect(jsonPath("$.violatedSlaPercent").value(40.0))
            .andExpect(jsonPath("$.averageFirstResponseMinutes").value(33.6))
            .andExpect(jsonPath("$.medianFirstResponseMinutes").value(25.0))
            .andExpect(jsonPath("$.percentile90FirstResponseMinutes").value(90.0));
    }

    @Test
    @DisplayName("GET /api/sla/b2c/summary - с фильтром по менеджеру")
    void shouldReturnB2CSummaryWithManagerFilter() throws Exception {
        B2CSummaryDto mockResponse = B2CSummaryDto.builder()
            .totalLeads(2)
            .withinSlaCount(2)
            .violatedSlaCount(0)
            .withinSlaPercent(100.0)
            .violatedSlaPercent(0.0)
            .averageFirstResponseMinutes(10.0)
            .medianFirstResponseMinutes(10.0)
            .percentile90FirstResponseMinutes(10.0)
            .minMinutes(5.0)
            .maxMinutes(15.0)
            .breachDistribution(new HashMap<>())
            .managerStats(new HashMap<>())
            .build();

        when(b2cSlaService.calculateSummary(any(LocalDate.class), any(LocalDate.class), eq("MANAGER_001"), any()))
            .thenReturn(mockResponse);

        mockMvc.perform(get("/api/sla/b2c/summary")
                .param("dateFrom", "2026-04-01")
                .param("dateTo", "2026-04-10")
                .param("managerId", "MANAGER_001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalLeads").value(2))
            .andExpect(jsonPath("$.withinSlaPercent").value(100.0));
    }

    @Test
    @DisplayName("GET /api/sla/b2c/summary - должен вернуть 400 при dateFrom > dateTo")
    void shouldReturnBadRequestWhenDateFromAfterDateTo() throws Exception {
        mockMvc.perform(get("/api/sla/b2c/summary")
                .param("dateFrom", "2026-04-10")
                .param("dateTo", "2026-04-01"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("dateFrom must be <= dateTo"));
    }

    @Test
    @DisplayName("GET /api/sla/b2c/summary - с опциональным параметром qualification")
    void shouldAcceptQualificationParameter() throws Exception {
        B2CSummaryDto mockResponse = B2CSummaryDto.builder()
            .totalLeads(3)
            .withinSlaCount(2)
            .violatedSlaCount(1)
            .withinSlaPercent(66.7)
            .violatedSlaPercent(33.3)
            .averageFirstResponseMinutes(25.0)
            .medianFirstResponseMinutes(20.0)
            .percentile90FirstResponseMinutes(45.0)
            .minMinutes(5.0)
            .maxMinutes(50.0)
            .breachDistribution(new HashMap<>())
            .managerStats(new HashMap<>())
            .build();

        when(b2cSlaService.calculateSummary(any(LocalDate.class), any(LocalDate.class), any(), eq("A")))
            .thenReturn(mockResponse);

        mockMvc.perform(get("/api/sla/b2c/summary")
                .param("dateFrom", "2026-04-01")
                .param("dateTo", "2026-04-10")
                .param("qualification", "A"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalLeads").value(3));
    }
}
