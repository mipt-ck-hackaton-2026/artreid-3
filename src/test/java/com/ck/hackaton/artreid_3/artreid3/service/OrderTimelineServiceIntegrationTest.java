package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.dto.OrderTimelineResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.OrderTimelineStepDTO;
import com.ck.hackaton.artreid_3.artreid3.model.Lead;
import com.ck.hackaton.artreid_3.artreid3.model.LeadEvent;
import com.ck.hackaton.artreid_3.artreid3.model.StageName;
import com.ck.hackaton.artreid_3.artreid3.repository.LeadEventRepository;
import com.ck.hackaton.artreid_3.artreid3.repository.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OrderTimelineServiceIntegrationTest {

    @Autowired
    private OrderTimelineService orderTimelineService;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private LeadEventRepository leadEventRepository;

    @MockitoBean
    private BuildProperties buildProperties;

    private Lead testLead;

    @BeforeEach
    void setUp() {
        leadEventRepository.deleteAll();
        leadRepository.deleteAll();

        testLead = new Lead();
        testLead.setExternalLeadId("EXT-TIMELINE-001");
        testLead.setManagerId("manager1");
        testLead = leadRepository.save(testLead);
    }

    @Test
    void getTimelineResponse_shouldReturnFullTimeline_whenMultipleEventsExist() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 1, 10, 10, 0);

        createEvent(testLead, StageName.CREATED, baseTime);
        createEvent(testLead, StageName.SALE, baseTime.plusMinutes(20));
        createEvent(testLead, StageName.TO_ASSEMBLY, baseTime.plusHours(3));
        createEvent(testLead, StageName.HANDED_TO_DELIVERY, baseTime.plusDays(1));
        createEvent(testLead, StageName.ISSUED_OR_PVZ, baseTime.plusDays(4));
        createEvent(testLead, StageName.RECEIVED, baseTime.plusDays(5));

        OrderTimelineResponseDTO response = orderTimelineService.getTimelineResponse(testLead.getLeadId());

        assertThat(response.getPipeline()).isEqualTo("lead");
        assertThat(response.getPeriod()).isNotNull();
        assertThat(response.getPeriod().getFrom()).isEqualTo(baseTime.toString());
        assertThat(response.getData()).hasSize(6); // 5 transitions + 1 last stage

        // Проверяем первый переход: CREATED -> SALE (20 минут)
        OrderTimelineStepDTO firstStep = response.getData().get(0);
        assertThat(firstStep.getStage()).isEqualTo(StageName.CREATED);
        assertThat(firstStep.getStartTime()).isEqualTo(baseTime);
        assertThat(firstStep.getEndTime()).isEqualTo(baseTime.plusMinutes(20));
        assertThat(firstStep.getDurationMinutes()).isEqualTo(20L);
        assertThat(firstStep.isSlaViolated()).isFalse(); // 20 мин < 30 мин SLA

        // Проверяем последний элемент — текущая стадия без endTime
        OrderTimelineStepDTO lastStep = response.getData().get(5);
        assertThat(lastStep.getStage()).isEqualTo(StageName.RECEIVED);
        assertThat(lastStep.getStartTime()).isEqualTo(baseTime.plusDays(5));
        assertThat(lastStep.getEndTime()).isNull();
        assertThat(lastStep.getDurationMinutes()).isNull();
    }

    @Test
    void getTimelineResponse_shouldDetectSlaViolation_whenReactionTimeExceeded() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 2, 1, 9, 0);

        createEvent(testLead, StageName.CREATED, baseTime);
        createEvent(testLead, StageName.SALE, baseTime.plusMinutes(45)); // > 30 мин SLA

        OrderTimelineResponseDTO response = orderTimelineService.getTimelineResponse(testLead.getLeadId());

        assertThat(response.getData()).hasSize(2);
        OrderTimelineStepDTO step = response.getData().get(0);
        assertThat(step.getStage()).isEqualTo(StageName.CREATED);
        assertThat(step.getDurationMinutes()).isEqualTo(45L);
        assertThat(step.isSlaViolated()).isTrue();
    }

    @Test
    void getTimelineResponse_shouldNotViolateSla_whenWithinThreshold() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 2, 1, 9, 0);

        createEvent(testLead, StageName.CREATED, baseTime);
        createEvent(testLead, StageName.SALE, baseTime.plusMinutes(25)); // < 30 мин SLA

        OrderTimelineResponseDTO response = orderTimelineService.getTimelineResponse(testLead.getLeadId());

        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().get(0).isSlaViolated()).isFalse();
    }

    @Test
    void getTimelineResponse_shouldReturnEmptyData_whenNoEventsExist() {
        OrderTimelineResponseDTO response = orderTimelineService.getTimelineResponse(testLead.getLeadId());

        assertThat(response.getPipeline()).isEqualTo("lead");
        assertThat(response.getData()).isEmpty();
        assertThat(response.getPeriod()).isNull();
    }

    @Test
    void getTimelineResponse_shouldThrowException_whenLeadNotFound() {
        Long nonExistentLeadId = 999999L;

        assertThatThrownBy(() -> orderTimelineService.getTimelineResponse(nonExistentLeadId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lead not found");
    }

    @Test
    void getTimelineResponse_shouldReturnSingleStep_whenOnlyOneEventExists() {
        LocalDateTime eventTime = LocalDateTime.of(2026, 3, 1, 12, 0);
        createEvent(testLead, StageName.CREATED, eventTime);

        OrderTimelineResponseDTO response = orderTimelineService.getTimelineResponse(testLead.getLeadId());

        assertThat(response.getData()).hasSize(1);
        OrderTimelineStepDTO step = response.getData().get(0);
        assertThat(step.getStage()).isEqualTo(StageName.CREATED);
        assertThat(step.getStartTime()).isEqualTo(eventTime);
        assertThat(step.getEndTime()).isNull();
        assertThat(step.getDurationMinutes()).isNull();
    }

    @Test
    void getTimelineResponse_shouldOrderEventsByTime_regardlessOfInsertionOrder() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 4, 1, 8, 0);

        // Вставляем в обратном порядке
        createEvent(testLead, StageName.SALE, baseTime.plusHours(1));
        createEvent(testLead, StageName.CREATED, baseTime);
        createEvent(testLead, StageName.TO_ASSEMBLY, baseTime.plusHours(5));

        OrderTimelineResponseDTO response = orderTimelineService.getTimelineResponse(testLead.getLeadId());

        assertThat(response.getData()).hasSize(3);
        assertThat(response.getData().get(0).getStage()).isEqualTo(StageName.CREATED);
        assertThat(response.getData().get(1).getStage()).isEqualTo(StageName.SALE);
        assertThat(response.getData().get(2).getStage()).isEqualTo(StageName.TO_ASSEMBLY);
    }

    @Test
    void getTimeline_shouldCalculateDurationDaysCorrectly() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 5, 1, 0, 0);

        createEvent(testLead, StageName.CREATED, baseTime);
        createEvent(testLead, StageName.SALE, baseTime.plusDays(1)); // ровно 1 день = 1440 мин

        List<OrderTimelineStepDTO> timeline = orderTimelineService.getTimeline(testLead.getLeadId());

        assertThat(timeline).hasSize(2);
        assertThat(timeline.get(0).getDurationMinutes()).isEqualTo(1440L);
        assertThat(timeline.get(0).getDurationDays()).isEqualTo(1.0);
    }

    private void createEvent(Lead lead, StageName stageName, LocalDateTime eventTime) {
        LeadEvent event = new LeadEvent();
        event.setLead(lead);
        event.setStageName(stageName);
        event.setEventTime(eventTime);
        leadEventRepository.save(event);
    }
}
