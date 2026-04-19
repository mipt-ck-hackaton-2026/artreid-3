package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.model.Lead;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderTimelineControllerIntegrationTest {

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
    void getOrderTimeline_withValidId_returnsTimeline() throws Exception {
        List<Lead> leads = leadRepository.findAll();
        Long leadId = leads.get(0).getLeadId();

        mockMvc.perform(get("/api/orders/{leadId}/timeline", leadId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.pipeline").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    void getOrderTimeline_withInvalidIdFormat_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/orders/{leadId}/timeline", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrderTimeline_whenEmptyTimeline_returnsEmptyData() throws Exception {
        Lead lead = new Lead();
        lead.setExternalLeadId("test-no-events");
        lead = leadRepository.save(lead);
        
        mockMvc.perform(get("/api/orders/{leadId}/timeline", lead.getLeadId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
