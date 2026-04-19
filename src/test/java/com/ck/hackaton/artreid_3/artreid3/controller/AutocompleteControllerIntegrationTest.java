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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
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
        lead3.setManagerId("manager alpha"); // Lowercase to test ILIKE
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
    void getManagers_withSpecificQuery_returnsMatching() throws Exception {
        mockMvc.perform(get("/api/autocomplete/managers").param("query", "Manager B"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]").value("Manager Beta"));
    }

    @Test
    void getCities_returnsCities() throws Exception {
        mockMvc.perform(get("/api/autocomplete/cities").param("query", "Mos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]").value("Moscow"));
    }

    @Test
    void getLeads_returnsLeadIds() throws Exception {
        mockMvc.perform(get("/api/autocomplete/leads").param("query", "LEAD-"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$", containsInAnyOrder("LEAD-1", "LEAD-2", "LEAD-3")));
    }
}
