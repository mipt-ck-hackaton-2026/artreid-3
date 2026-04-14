package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.dto.DeliverySummaryResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerDeliverySlaResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.SlaDeliveryRequestDTO;
import com.ck.hackaton.artreid_3.artreid3.repository.DeliveryMetricsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlaMetricsServiceTest {

        @Mock
        private DeliveryMetricsRepository slaMetricsRepository;

        @Mock
        private SlaConfig slaConfig;

        @InjectMocks
        private DeliveryMetricsService slaMetricsService;

        @Test
        void shouldUseDefaultDateRange_whenDatesNotProvided() {
                SlaDeliveryRequestDTO request = SlaDeliveryRequestDTO.builder().build();
                when(slaConfig.getToPvzDays()).thenReturn(5);
                when(slaConfig.getPvzStorageDays()).thenReturn(7);
                when(slaConfig.getDeliveryTotalDays()).thenReturn(14);
                when(slaMetricsRepository.findDeliverySlaByManager(any(), any(), isNull(), isNull(), isNull(), anyInt(),
                                anyInt(), anyInt()))
                                .thenReturn(List.of());

                LocalDateTime testStartTime = LocalDateTime.now();

                slaMetricsService.getDeliverySlaByManager(request);

                ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
                ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

                verify(slaMetricsRepository).findDeliverySlaByManager(
                                fromCaptor.capture(), toCaptor.capture(), isNull(), isNull(), isNull(), eq(7200),
                                eq(10080), eq(20160));

                LocalDateTime capturedFrom = fromCaptor.getValue();
                LocalDateTime capturedTo = toCaptor.getValue();

                assertThat(capturedFrom).isBefore(testStartTime.minusDays(29));
                assertThat(capturedFrom).isAfter(testStartTime.minusDays(31));

                assertThat(capturedTo).isBetween(
                                testStartTime.minusSeconds(1),
                                testStartTime.plusSeconds(1));
        }

        @Test
        void shouldCallRepository_forSummary() {
                SlaDeliveryRequestDTO request = SlaDeliveryRequestDTO.builder().build();
                when(slaConfig.getToPvzDays()).thenReturn(5);
                when(slaConfig.getPvzStorageDays()).thenReturn(7);
                when(slaConfig.getDeliveryTotalDays()).thenReturn(14);

                DeliverySummaryResponseDTO.DeliverySummaryMetrics metrics = DeliverySummaryResponseDTO.DeliverySummaryMetrics
                                .builder().build();
                when(slaMetricsRepository.findDeliverySummary(any(), any(), isNull(), isNull(), isNull(), anyInt(),
                                anyInt(), anyInt()))
                                .thenReturn(metrics);

                slaMetricsService.getDeliverySummary(request);

                verify(slaMetricsRepository).findDeliverySummary(any(), any(), isNull(), isNull(), isNull(), eq(7200),
                                eq(10080), eq(20160));
        }
}
