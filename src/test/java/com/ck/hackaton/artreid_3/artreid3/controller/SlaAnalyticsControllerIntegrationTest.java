package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.repository.LeadEventRepository;
import com.ck.hackaton.artreid_3.artreid3.repository.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SlaAnalyticsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private LeadEventRepository leadEventRepository;

    @MockitoBean
    private BuildProperties buildProperties;

    @BeforeEach
    void setupDatabase() throws Exception {
        leadEventRepository.deleteAll();
        leadRepository.deleteAll();

        ClassPathResource resource = new ClassPathResource("test-data/dataset-fragment.csv");
        byte[] csvBytes = Files.readAllBytes(resource.getFile().toPath());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "dataset-fragment.csv",
                "text/csv",
                csvBytes
        );

        mockMvc.perform(multipart("/api/data/load").file(file))
                .andExpect(status().isCreated());
    }

    // B2C Sla Tests

    @Test
    void getB2CSummary_returnsSummary() throws Exception {
        mockMvc.perform(get("/api/sla/b2c/summary")
                        .param("dateFrom", "2025-02-01")
                        .param("dateTo", "2025-04-01"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pipeline").value("b2c"))
                .andExpect(jsonPath("$.metrics.sla1_reaction.threshold_minutes").value(30))
                .andExpect(jsonPath("$.metrics.sla1_reaction.total_orders").value(1))
                .andExpect(jsonPath("$.metrics.sla2_to_assembly.threshold_minutes").value(240))
                .andExpect(jsonPath("$.metrics.sla2_to_assembly.total_orders").value(1))
                .andExpect(jsonPath("$.metrics.sla3_to_delivery.threshold_minutes").value(1440))
                .andExpect(jsonPath("$.metrics.sla3_to_delivery.total_orders").value(1))
                .andExpect(jsonPath("$.metrics.b2c_total.threshold_minutes").value(2880))
                .andExpect(jsonPath("$.metrics.b2c_total.total_orders").value(1));
    }

    @Test
    void getB2CSummary_dateFromAfterDateTo_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/sla/b2c/summary")
                        .param("dateFrom", "2026-03-31")
                        .param("dateTo", "2026-03-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("dateFrom must be <= dateTo"));
    }

    @Test
    void getB2CSummary_invalidDateFormat_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/sla/b2c/summary")
                        .param("dateFrom", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getB2CSlaByManager_returnsDataGroupedByManager() throws Exception {
        mockMvc.perform(get("/api/sla/b2c/by-manager")
                        .param("dateFrom", "2025-02-01")
                        .param("dateTo", "2025-04-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pipeline").value("b2c"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data[0].manager_id").exists())
                .andExpect(jsonPath("$.data[0].metrics.sla1_reaction.threshold_minutes").value(30))
                .andExpect(jsonPath("$.data[0].metrics.sla1_reaction.total_orders").value(1))
                .andExpect(jsonPath("$.data[0].metrics.sla2_to_assembly.threshold_minutes").value(240))
                .andExpect(jsonPath("$.data[0].metrics.sla2_to_assembly.total_orders").value(1))
                .andExpect(jsonPath("$.data[0].metrics.sla3_to_delivery.threshold_minutes").value(1440))
                .andExpect(jsonPath("$.data[0].metrics.sla3_to_delivery.total_orders").value(1))
                .andExpect(jsonPath("$.data[0].metrics.b2c_total.threshold_minutes").value(2880))
                .andExpect(jsonPath("$.data[0].metrics.b2c_total.total_orders").value(1));
    }

    @Test
    void getB2CSlaByManager_dateFromAfterDateTo_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/sla/b2c/by-manager")
                        .param("dateFrom", "2026-03-31")
                        .param("dateTo", "2026-03-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("dateFrom must be <= dateTo"));
    }

    @Test
    void getFullSummary_returnsFullCycleSummary() throws Exception {
        mockMvc.perform(get("/api/sla/full/summary")
                        .param("dateFrom", "2025-02-01")
                        .param("dateTo", "2025-04-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pipeline").value("full"))
                .andExpect(jsonPath("$.period.from").value("2025-02-01"))
                .andExpect(jsonPath("$.period.to").value("2025-04-01"))
                .andExpect(jsonPath("$.metrics.full_total.threshold_minutes").value(16))
                .andExpect(jsonPath("$.metrics.full_total.total_orders").value(1))
                .andExpect(jsonPath("$.metrics.full_total.met_count").value(0))
                .andExpect(jsonPath("$.metrics.full_total.met_percent").value(0.0))
                .andExpect(jsonPath("$.metrics.full_total.breach_count").value(1))
                .andExpect(jsonPath("$.metrics.full_total.breach_percent").value(100.0))
                .andExpect(jsonPath("$.metrics.full_total.avg_minutes").value(16359.18))
                .andExpect(jsonPath("$.metrics.full_total.median_minutes").value(16359.18))
                .andExpect(jsonPath("$.metrics.full_total.p90_minutes").value(16359.18))
                .andExpect(jsonPath("$.metrics.full_total.breach_distribution.metadata.unit").value("day"))
                .andExpect(jsonPath("$.metrics.full_total.breach_distribution.metadata.total_count").value(1))
                .andExpect(jsonPath("$.metrics.full_total.breach_distribution.items[0].sort_order").value(1))
                .andExpect(jsonPath("$.metrics.full_total.breach_distribution.items[0].min_bound").value(0))
                .andExpect(jsonPath("$.metrics.full_total.breach_distribution.items[0].max_bound").value(1))
                .andExpect(jsonPath("$.metrics.full_total.breach_distribution.items[0].count").value(0))
                .andExpect(jsonPath("$.metrics.full_total.breach_distribution.items[0].ratio").value(0.0))
                .andExpect(jsonPath("$.metrics.full_total.breach_distribution.items[1].sort_order").value(2))
                .andExpect(jsonPath("$.metrics.full_total.breach_distribution.items[1].min_bound").value(1))
                .andExpect(jsonPath("$.metrics.full_total.breach_distribution.items[1].max_bound").value(3))
                .andExpect(jsonPath("$.metrics.full_total.breach_distribution.items[1].count").value(0))
                .andExpect(jsonPath("$.metrics.full_total.breach_distribution.items[1].ratio").value(0.0))
                .andExpect(jsonPath("$.metrics.full_total.breach_distribution.items[2].sort_order").value(3))
                .andExpect(jsonPath("$.metrics.full_total.breach_distribution.items[2].min_bound").value(3))
                .andExpect(jsonPath("$.metrics.full_total.breach_distribution.items[2].max_bound").isEmpty())
                .andExpect(jsonPath("$.metrics.full_total.breach_distribution.items[2].count").value(1))
                .andExpect(jsonPath("$.metrics.full_total.breach_distribution.items[2].ratio").value(1.0));
    }

    @Test
    void getFullSummary_dateFromAfterDateTo_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/sla/full/summary")
                        .param("dateFrom", "2026-03-31")
                        .param("dateTo", "2026-03-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("dateFrom must be <= dateTo"));
    }

    @Test
    void getFullSummary_invalidDateFormat_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/sla/full/summary")
                        .param("dateFrom", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    // Delivery Sla Tests

    @Test
    void getDeliverySlaByManager_withValidDates_returnsMetrics() throws Exception {
        mockMvc.perform(get("/api/sla/delivery/by-manager")
                        .param("dateFrom", "2025-02-01T00:00:00")
                        .param("dateTo", "2025-04-01T23:59:59"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.pipeline").value("delivery"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data[0].metrics.sla4_to_pvz.threshold_minutes").value(7200))
                .andExpect(jsonPath("$.data[0].metrics.sla4_to_pvz.total_orders").value(1))
                .andExpect(jsonPath("$.data[0].metrics.sla4_to_pvz.breach_count").value(1))
                .andExpect(jsonPath("$.data[0].metrics.sla5_at_pvz.threshold_minutes").value(10080))
                .andExpect(jsonPath("$.data[0].metrics.sla5_at_pvz.total_orders").value(1))
                .andExpect(jsonPath("$.data[0].metrics.sla5_at_pvz.met_count").value(1))
                .andExpect(jsonPath("$.data[0].metrics.delivery_total.threshold_minutes").value(20160))
                .andExpect(jsonPath("$.data[0].metrics.delivery_total.met_count").value(1));
    }

    @Test
    void getDeliverySummary_withValidDates_returnsSummary() throws Exception {
        mockMvc.perform(get("/api/sla/delivery/summary")
                        .param("dateFrom", "2025-02-01T00:00:00")
                        .param("dateTo", "2025-04-01T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.pipeline").value("delivery"))
                .andExpect(jsonPath("$.metrics").exists())
                .andExpect(jsonPath("$.metrics.sla4_to_pvz.threshold_minutes").value(7200))
                .andExpect(jsonPath("$.metrics.sla4_to_pvz.total_orders").value(1))
                .andExpect(jsonPath("$.metrics.sla5_at_pvz.threshold_minutes").value(10080))
                .andExpect(jsonPath("$.metrics.sla5_at_pvz.total_orders").value(1))
                .andExpect(jsonPath("$.metrics.delivery_total.threshold_minutes").value(20160))
                .andExpect(jsonPath("$.metrics.delivery_total.total_orders").value(1));
    }

    @Test
    void getDeliverySlaByManager_withInvalidDateFormat_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/sla/delivery/by-manager")
                        .param("dateFrom", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDeliverySlaByManager_dateFromAfterDateTo_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/sla/delivery/by-manager")
                        .param("dateFrom", "2026-03-31T00:00:00")
                        .param("dateTo", "2026-03-01T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("dateFrom must be <= dateTo"));
    }

    @Test
    void getDeliverySummary_dateFromAfterDateTo_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/sla/delivery/summary")
                        .param("dateFrom", "2026-03-31T00:00:00")
                        .param("dateTo", "2026-03-01T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("dateFrom must be <= dateTo"));
    }
}
