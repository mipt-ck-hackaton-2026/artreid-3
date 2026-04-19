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
                .andExpect(jsonPath("$.b2c").exists())
                .andExpect(jsonPath("$.delivery").exists())
                .andExpect(jsonPath("$.breach_buckets").exists())
                .andExpect(jsonPath("$.full_cycle_days").exists())
                .andExpect(jsonPath("$.$$beanFactory").doesNotExist())
                .andExpect(jsonPath("$.targetSource").doesNotExist());
    }
}
