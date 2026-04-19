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
class SlaMetricsControllerIntegrationTest {

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

    @Test
    void getDeliverySlaByManager_withValidDates_returnsMetrics() throws Exception {
        mockMvc.perform(get("/api/sla/delivery/by-manager")
                        .param("dateFrom", "2025-02-01T00:00:00")
                        .param("dateTo", "2025-04-01T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.pipeline").value("delivery"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isNotEmpty());
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
                .andExpect(jsonPath("$.metrics.sla4_to_pvz").exists())
                .andExpect(jsonPath("$.metrics.sla5_at_pvz").exists())
                .andExpect(jsonPath("$.metrics.delivery_total").exists());
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
