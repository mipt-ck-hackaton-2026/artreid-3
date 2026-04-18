package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.dto.OrderTimelineResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.OrderTimelineStepDTO;
import com.ck.hackaton.artreid_3.artreid3.model.StageName;
import com.ck.hackaton.artreid_3.artreid3.service.OrderTimelineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderTimelineControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderTimelineService timelineService;

    @Test
    void getOrderTimeline_withValidId_returnsTimeline() throws Exception {
        OrderTimelineStepDTO step1 = OrderTimelineStepDTO.builder()
                .stage(StageName.CREATED)
                .startTime(LocalDateTime.parse("2026-04-18T10:00:00"))
                .endTime(LocalDateTime.parse("2026-04-18T10:30:00"))
                .durationMinutes(30L)
                .durationDays(30.0 / 1440)
                .slaViolated(false)
                .build();
        
        OrderTimelineStepDTO step2 = OrderTimelineStepDTO.builder()
                .stage(StageName.SALE)
                .startTime(LocalDateTime.parse("2026-04-18T10:30:00"))
                .build();

        OrderTimelineResponseDTO response = OrderTimelineResponseDTO.builder()
                .period(OrderTimelineResponseDTO.PeriodDto.builder()
                        .from("2026-04-18T10:00:00")
                        .to("2026-04-18T10:30:00")
                        .build())
                .pipeline("lead")
                .data(List.of(step1, step2))
                .build();

        when(timelineService.getTimelineResponse(123L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/{leadId}/timeline", 123L))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.pipeline").value("lead"))
                .andExpect(jsonPath("$.period.from").value("2026-04-18T10:00:00"))
                .andExpect(jsonPath("$.period.to").value("2026-04-18T10:30:00"))
                .andExpect(jsonPath("$.data[0].stage").value("CREATED"))
                .andExpect(jsonPath("$.data[0].durationMinutes").value(30))
                .andExpect(jsonPath("$.data[0].slaViolated").value(false))
                .andExpect(jsonPath("$.data[1].stage").value("SALE"));
    }

    @Test
    void getOrderTimeline_withInvalidIdFormat_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/orders/{leadId}/timeline", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrderTimeline_whenEmptyTimeline_returnsEmptyData() throws Exception {
        OrderTimelineResponseDTO response = OrderTimelineResponseDTO.builder()
                .pipeline("lead")
                .data(List.of())
                .build();

        when(timelineService.getTimelineResponse(456L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/{leadId}/timeline", 456L))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.pipeline").value("lead"))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
