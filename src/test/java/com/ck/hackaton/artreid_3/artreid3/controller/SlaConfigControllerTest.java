package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SlaConfigController.class)
class SlaConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SlaConfig slaConfig;

    @MockitoBean
    private BuildProperties buildProperties;

    @Test
    void getConfig_returnsSlaConfig() throws Exception {
        when(slaConfig.getFirstResponseNormativeMinutes()).thenReturn(45);

        mockMvc.perform(get("/api/sla/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstResponseNormativeMinutes").value(45));
    }
}
