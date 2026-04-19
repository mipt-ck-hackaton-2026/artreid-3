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
        String externalLeadId = leads.get(0).getExternalLeadId();

        mockMvc.perform(get("/api/orders/{leadId}/timeline", externalLeadId))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.pipeline").value("lead"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data[0].stage").value("CREATED"))
                .andExpect(jsonPath("$.data[0].slaViolated").value(true))
                .andExpect(jsonPath("$.data[1].stage").value("SALE"))
                .andExpect(jsonPath("$.data[1].slaViolated").value(true))
                .andExpect(jsonPath("$.data[2].stage").value("HANDED_TO_DELIVERY"))
                .andExpect(jsonPath("$.data[2].slaViolated").value(true));
    }

    @Test
    void getOrderTimeline_whenEmptyTimeline_returnsEmptyData() throws Exception {
        Lead lead = new Lead();
        lead.setExternalLeadId("test-no-events");
        lead = leadRepository.save(lead);
        
        mockMvc.perform(get("/api/orders/{leadId}/timeline", lead.getExternalLeadId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getOrderTimeline_withNonExistentId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/orders/{leadId}/timeline", "NON_EXISTENT_LEAD_ID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Lead not found: NON_EXISTENT_LEAD_ID"));
    }
}
