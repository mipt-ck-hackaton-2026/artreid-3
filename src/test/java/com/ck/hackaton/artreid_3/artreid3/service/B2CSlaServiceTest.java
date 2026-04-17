package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.model.B2CSummaryDto;
import com.ck.hackaton.artreid_3.artreid3.model.Lead;
import com.ck.hackaton.artreid_3.artreid3.model.LeadEvent;
import com.ck.hackaton.artreid_3.artreid3.repository.LeadEventRepository;
import com.ck.hackaton.artreid_3.artreid3.repository.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты B2C SLA сервиса")
class B2CSlaServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private LeadEventRepository leadEventRepository;

    @Mock
    private SlaConfig slaConfig;

    @InjectMocks
    private B2CSlaService b2cSlaService;

    private Lead testLead1;
    private Lead testLead2;
    private LeadEvent createdEvent;
    private LeadEvent saleEvent;

    @BeforeEach
    void setUp() {
        testLead1 = new Lead();
        testLead1.setLeadId(1L);
        testLead1.setExternalLeadId("LEAD_001");
        testLead1.setManagerId("MANAGER_001");

        testLead2 = new Lead();
        testLead2.setLeadId(2L);
        testLead2.setExternalLeadId("LEAD_002");
        testLead2.setManagerId("MANAGER_002");

        createdEvent = new LeadEvent();
        createdEvent.setStageName("lead_created");
        createdEvent.setEventTime(LocalDateTime.of(2026, 4, 1, 10, 0, 0));

        saleEvent = new LeadEvent();
        saleEvent.setStageName("sale");
        saleEvent.setEventTime(LocalDateTime.of(2026, 4, 1, 10, 30, 0));
    }

    @Test
    @DisplayName("Должен возвращать пустую статистику при отсутствии данных")
    void shouldReturnEmptySummaryWhenNoData() {
        when(leadRepository.findAll()).thenReturn(Collections.emptyList());

        B2CSummaryDto result = b2cSlaService.calculateSummary(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                null,
                null
        );

        assertNotNull(result);
        assertEquals(0, result.getTotalLeads());
        assertEquals(0.0, result.getAverageFirstResponseMinutes());
    }

    @Test
    @DisplayName("Должен корректно рассчитывать статистику для одного лида (в норме)")
    void shouldCalculateStatisticsForOneLeadWithinSla() {
        when(leadRepository.findAll()).thenReturn(List.of(testLead1));
        when(leadEventRepository.findByLeadIdAndStageNames(any(Long.class), anyList()))
                .thenReturn(List.of(createdEvent, saleEvent));
        when(slaConfig.getFirstResponseNormativeMinutes()).thenReturn(30);

        B2CSummaryDto result = b2cSlaService.calculateSummary(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                null,
                null
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalLeads());
        assertEquals(1, result.getWithinSlaCount());
        assertEquals(0, result.getViolatedSlaCount());
        assertEquals(100.0, result.getWithinSlaPercent());
        assertEquals(0.0, result.getViolatedSlaPercent());
        assertEquals(30.0, result.getAverageFirstResponseMinutes());
        assertEquals(30.0, result.getMedianFirstResponseMinutes());
        assertEquals(30.0, result.getPercentile90FirstResponseMinutes());
    }

    @Test
    @DisplayName("Должен корректно рассчитывать статистику для одного лида (нарушение)")
    void shouldCalculateStatisticsForOneLeadViolated() {
        when(leadRepository.findAll()).thenReturn(List.of(testLead1));
        when(leadEventRepository.findByLeadIdAndStageNames(any(Long.class), anyList()))
                .thenReturn(List.of(createdEvent, saleEvent));
        when(slaConfig.getFirstResponseNormativeMinutes()).thenReturn(10);

        B2CSummaryDto result = b2cSlaService.calculateSummary(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                null,
                null
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalLeads());
        assertEquals(0, result.getWithinSlaCount());
        assertEquals(1, result.getViolatedSlaCount());
        assertEquals(0.0, result.getWithinSlaPercent());
        assertEquals(100.0, result.getViolatedSlaPercent());
    }

    @Test
    @DisplayName("Должен фильтровать по менеджеру")
    void shouldFilterByManager() {
        when(leadRepository.findAll()).thenReturn(List.of(testLead1, testLead2));
        when(leadEventRepository.findByLeadIdAndStageNames(any(Long.class), anyList()))
                .thenReturn(List.of(createdEvent, saleEvent));
        when(slaConfig.getFirstResponseNormativeMinutes()).thenReturn(30);

        B2CSummaryDto result = b2cSlaService.calculateSummary(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                "MANAGER_001",
                null
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalLeads());
    }

    @Test
    @DisplayName("Должен игнорировать лиды вне периода дат")
    void shouldIgnoreLeadsOutsideDateRange() {
        when(leadRepository.findAll()).thenReturn(List.of(testLead1));
        when(leadEventRepository.findByLeadIdAndStageNames(any(Long.class), anyList()))
                .thenReturn(List.of(createdEvent, saleEvent));

        B2CSummaryDto result = b2cSlaService.calculateSummary(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                null,
                null
        );

        assertNotNull(result);
        assertEquals(0, result.getTotalLeads());
    }

    @Test
    @DisplayName("Должен корректно рассчитывать распределение нарушений")
    void shouldCalculateBreachDistribution() {
        when(leadRepository.findAll()).thenReturn(List.of(testLead1));
        when(leadEventRepository.findByLeadIdAndStageNames(any(Long.class), anyList()))
                .thenReturn(List.of(createdEvent, saleEvent));
        when(slaConfig.getFirstResponseNormativeMinutes()).thenReturn(10);

        B2CSummaryDto result = b2cSlaService.calculateSummary(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                null,
                null
        );

        assertNotNull(result);
        assertNotNull(result.getBreachDistribution());
        assertTrue(result.getBreachDistribution().containsKey("≤15 мин"));
        assertTrue(result.getBreachDistribution().containsKey("15-60 мин"));
        assertTrue(result.getBreachDistribution().containsKey("1-4 ч"));
        assertTrue(result.getBreachDistribution().containsKey(">4 ч"));
    }

    @Test
    @DisplayName("Должен корректно рассчитывать несколько лидов")
    void shouldCalculateStatisticsForMultipleLeads() {
        Lead lead3 = new Lead();
        lead3.setLeadId(3L);
        lead3.setExternalLeadId("LEAD_003");
        lead3.setManagerId("MANAGER_001");

        LeadEvent createdEvent2 = new LeadEvent();
        createdEvent2.setStageName("lead_created");
        createdEvent2.setEventTime(LocalDateTime.of(2026, 4, 2, 9, 0, 0));

        LeadEvent saleEvent2 = new LeadEvent();
        saleEvent2.setStageName("sale");
        saleEvent2.setEventTime(LocalDateTime.of(2026, 4, 2, 9, 5, 0)); // 5 минут

        when(leadRepository.findAll()).thenReturn(List.of(testLead1, lead3));
        when(leadEventRepository.findByLeadIdAndStageNames(any(Long.class), anyList()))
                .thenReturn(List.of(createdEvent, saleEvent))   // для LEAD_001: 30 мин
                .thenReturn(List.of(createdEvent2, saleEvent2)); // для LEAD_003: 5 мин
        when(slaConfig.getFirstResponseNormativeMinutes()).thenReturn(30);

        B2CSummaryDto result = b2cSlaService.calculateSummary(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                null,
                null
        );

        assertNotNull(result);
        assertEquals(2, result.getTotalLeads());
        assertEquals(2, result.getWithinSlaCount());
        assertEquals(0, result.getViolatedSlaCount());
        assertEquals(100.0, result.getWithinSlaPercent());
        assertEquals(17.5, result.getAverageFirstResponseMinutes()); // (30 + 5) / 2 = 17.5
    }
}