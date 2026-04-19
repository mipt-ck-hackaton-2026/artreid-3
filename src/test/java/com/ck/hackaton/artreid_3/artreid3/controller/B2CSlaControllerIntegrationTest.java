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
class B2CSlaControllerIntegrationTest {

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
    void getB2CSummary_returnsSummary() throws Exception {
        mockMvc.perform(get("/api/sla/b2c/summary")
                        .param("dateFrom", "2025-02-01")
                        .param("dateTo", "2025-04-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pipeline").value("b2c"));
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
    void getB2CSlaByManager_returnsDataGroupedByManager() throws Exception {
        mockMvc.perform(get("/api/sla/b2c/by-manager")
                        .param("dateFrom", "2025-02-01")
                        .param("dateTo", "2025-04-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pipeline").value("b2c"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data[0].manager_id").exists())
                .andExpect(jsonPath("$.data[0].metrics").exists());
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
                .andExpect(jsonPath("$.metrics").exists())
                .andExpect(jsonPath("$.metrics.full_total").exists());
    }
}
