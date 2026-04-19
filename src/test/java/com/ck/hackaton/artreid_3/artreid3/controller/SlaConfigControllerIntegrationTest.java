package com.ck.hackaton.artreid_3.artreid3.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SlaConfigControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BuildProperties buildProperties;

    @Test
    void getConfig_returnsSlaConfigAndDoesNotExposeSpringInternals() throws Exception {
        mockMvc.perform(get("/api/sla/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.b2c.reaction_minutes").value(30))
                .andExpect(jsonPath("$.b2c.to_assembly_hours").value(4))
                .andExpect(jsonPath("$.b2c.assembly_to_delivery_days").value(1))
                .andExpect(jsonPath("$.b2c.total_days").value(2))
                .andExpect(jsonPath("$.delivery.to_pvz_days").value(5))
                .andExpect(jsonPath("$.delivery.pvz_storage_days").value(7))
                .andExpect(jsonPath("$.delivery.total_days").value(14))
                .andExpect(jsonPath("$.breach_buckets.short_minutes[0]").value(15))
                .andExpect(jsonPath("$.breach_buckets.short_minutes[1]").value(60))
                .andExpect(jsonPath("$.breach_buckets.days[0]").value(1))
                .andExpect(jsonPath("$.breach_buckets.days[1]").value(3))
                .andExpect(jsonPath("$.full_cycle_days").value(16))
                .andExpect(jsonPath("$.$$beanFactory").doesNotExist())
                .andExpect(jsonPath("$.targetSource").doesNotExist());
    }
}
