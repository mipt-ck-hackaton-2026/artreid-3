package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.model.Lead;
import com.ck.hackaton.artreid_3.artreid3.repository.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AutocompleteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadRepository leadRepository;

    @MockitoBean
    private BuildProperties buildProperties;

    @BeforeEach
    void setUp() {
        leadRepository.deleteAll();
        
        Lead lead1 = new Lead();
        lead1.setExternalLeadId("LEAD-1");
        lead1.setManagerId("Manager Alpha");
        lead1.setLeadQualification("High");
        lead1.setDeliveryService("Service A");
        lead1.setCity("Moscow");
        lead1.setDeliveryManagerId("DM-1");
        
        Lead lead2 = new Lead();
        lead2.setExternalLeadId("LEAD-2");
        lead2.setManagerId("Manager Beta");
        lead2.setLeadQualification("Medium");
        lead2.setDeliveryService("Service B");
        lead2.setCity("Saint Petersburg");
        lead2.setDeliveryManagerId("DM-2");

        Lead lead3 = new Lead();
        lead3.setExternalLeadId("LEAD-3");
        lead3.setManagerId("manager alpha"); // For ILIKE test
        lead3.setLeadQualification("Low");
        lead3.setDeliveryService("Service A");
        lead3.setCity("Moscow");
        lead3.setDeliveryManagerId("DM-3");

        leadRepository.saveAll(List.of(lead1, lead2, lead3));
    }

    @Test
    void getManagers_returnsFilteredManagersCaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/autocomplete/managers").param("query", "manager"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$", containsInAnyOrder("Manager Alpha", "Manager Beta", "manager alpha")));
    }

    @Test
    void getQualifications_returnsFilteredQualifications() throws Exception {
        mockMvc.perform(get("/api/autocomplete/qualifications").param("query", "h"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]").value("High"));
    }

    @Test
    void getDeliveryServices_returnsDistinctServices() throws Exception {
        mockMvc.perform(get("/api/autocomplete/delivery-services").param("query", "Service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$", containsInAnyOrder("Service A", "Service B")));
    }

    @Test
    void getCities_returnsCities() throws Exception {
        mockMvc.perform(get("/api/autocomplete/cities").param("query", "Mos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]").value("Moscow"));
    }

    @Test
    void getDeliveryManagers_returnsMatching() throws Exception {
        mockMvc.perform(get("/api/autocomplete/delivery-managers").param("query", "DM-"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$", containsInAnyOrder("DM-1", "DM-2", "DM-3")));
    }

    @Test
    void getLeads_returnsLeadIds() throws Exception {
        mockMvc.perform(get("/api/autocomplete/leads").param("query", "LEAD-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]").value("LEAD-1"));
    }

    @Test
    void getManagers_withLimit_respectsLimit() throws Exception {
        mockMvc.perform(get("/api/autocomplete/managers")
                        .param("query", "manager")
                        .param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getManagers_withEmptyQuery_returnsAllUpToLimit() throws Exception {
        mockMvc.perform(get("/api/autocomplete/managers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }
}
