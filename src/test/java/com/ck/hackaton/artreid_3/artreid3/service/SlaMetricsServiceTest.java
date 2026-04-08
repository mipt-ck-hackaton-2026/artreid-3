package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.model.ManagerDeliverySlaMetrics;
import com.ck.hackaton.artreid_3.artreid3.model.SlaDeliveryRequest;
import com.ck.hackaton.artreid_3.artreid3.repository.SlaMetricsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlaMetricsServiceTest {

    @Mock
    private SlaMetricsRepository slaMetricsRepository;

    @Mock
    private SlaConfig slaConfig;

    @InjectMocks
    private SlaMetricsService slaMetricsService;

    @Test
    void shouldUseDefaultDateRange_whenDatesNotProvided() {
        SlaDeliveryRequest request = new SlaDeliveryRequest(null, null, null);
        when(slaConfig.getDeliverySlaThresholdMinutes()).thenReturn(20160);
        when(slaMetricsRepository.findDeliverySlaByManager(any(), any(), isNull(), eq(20160)))
                .thenReturn(List.of());

        slaMetricsService.getDeliverySlaByManager(request);

        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(slaMetricsRepository).findDeliverySlaByManager(
                fromCaptor.capture(), toCaptor.capture(), isNull(), eq(20160)
        );

        LocalDateTime capturedFrom = fromCaptor.getValue();
        LocalDateTime capturedTo = toCaptor.getValue();

        assertThat(capturedFrom).isBefore(LocalDateTime.now().minusDays(29));
        assertThat(capturedFrom).isAfter(LocalDateTime.now().minusDays(31));
        assertThat(capturedTo).isAfterOrEqualTo(LocalDateTime.now());
    }

    @Test
    void shouldUseCustomDates_whenProvided() {
        LocalDateTime customFrom = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime customTo = LocalDateTime.of(2026, 1, 31, 23, 59);
        SlaDeliveryRequest request = new SlaDeliveryRequest(customFrom, customTo, null);

        when(slaConfig.getDeliverySlaThresholdMinutes()).thenReturn(20160);
        when(slaMetricsRepository.findDeliverySlaByManager(eq(customFrom), eq(customTo), isNull(), eq(20160)))
                .thenReturn(List.of());

        slaMetricsService.getDeliverySlaByManager(request);

        verify(slaMetricsRepository).findDeliverySlaByManager(
                eq(customFrom), eq(customTo), isNull(), eq(20160)
        );
    }

    @Test
    void shouldPassSlaThreshold_fromConfig() {
        SlaDeliveryRequest request = new SlaDeliveryRequest(
                LocalDateTime.now(), LocalDateTime.now(), null);
        when(slaConfig.getDeliverySlaThresholdMinutes()).thenReturn(14400); // 10 дней в минутах
        when(slaMetricsRepository.findDeliverySlaByManager(any(), any(), isNull(), eq(14400)))
                .thenReturn(List.of());

        slaMetricsService.getDeliverySlaByManager(request);

        verify(slaMetricsRepository).findDeliverySlaByManager(
                any(), any(), isNull(), eq(14400)
        );
    }

    @Test
    void shouldPassManagerId_toRepository() {
        String managerId = "mgr_456";
        SlaDeliveryRequest request = new SlaDeliveryRequest(
                LocalDateTime.now(), LocalDateTime.now(), managerId);
        when(slaConfig.getDeliverySlaThresholdMinutes()).thenReturn(20160);
        when(slaMetricsRepository.findDeliverySlaByManager(any(), any(), eq(managerId), eq(20160)))
                .thenReturn(List.of());

        slaMetricsService.getDeliverySlaByManager(request);

        verify(slaMetricsRepository).findDeliverySlaByManager(
                any(), any(), eq(managerId), eq(20160)
        );
    }

    @Test
    void shouldReturnRepositoryResult() {
        ManagerDeliverySlaMetrics expected = new ManagerDeliverySlaMetrics(
                "mgr_test", 5L, new BigDecimal("120.00"), new BigDecimal("100.00"),
                new BigDecimal("200.00"), 4L, new BigDecimal("80.00")
        );
        when(slaConfig.getDeliverySlaThresholdMinutes()).thenReturn(20160);
        when(slaMetricsRepository.findDeliverySlaByManager(any(), any(), any(), eq(20160)))
                .thenReturn(List.of(expected));

        List<ManagerDeliverySlaMetrics> result = slaMetricsService.getDeliverySlaByManager(
                new SlaDeliveryRequest(LocalDateTime.now(), LocalDateTime.now(), null)
        );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().managerId()).isEqualTo("mgr_test");
        assertThat(result.getFirst().withinSlaPercent()).isEqualByComparingTo("80.00");
    }

    @Test
    void shouldHandleEmptyResult_fromRepository() {
        when(slaConfig.getDeliverySlaThresholdMinutes()).thenReturn(20160);
        when(slaMetricsRepository.findDeliverySlaByManager(any(), any(), any(), eq(20160)))
                .thenReturn(List.of());

        List<ManagerDeliverySlaMetrics> result = slaMetricsService.getDeliverySlaByManager(
                new SlaDeliveryRequest(LocalDateTime.now(), LocalDateTime.now(), null)
        );

        assertThat(result).isEmpty();
    }
}