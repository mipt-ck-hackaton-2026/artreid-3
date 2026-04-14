package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.dto.DeliverySummaryResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerDeliverySlaResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.SlaDeliveryRequestDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerDeliverySlaResponseDTO.ManagerDeliveryData;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerDeliverySlaResponseDTO.Period;
import com.ck.hackaton.artreid_3.artreid3.repository.DeliveryMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryMetricsService {

        private final DeliveryMetricsRepository slaMetricsRepository;
        private final SlaConfig slaConfig;

        public ManagerDeliverySlaResponseDTO getDeliverySlaByManager(SlaDeliveryRequestDTO request) {
                int sla4Threshold = slaConfig.getToPvzDays() * 24 * 60;
                int sla5Threshold = slaConfig.getPvzStorageDays() * 24 * 60;
                int delThreshold = slaConfig.getDeliveryTotalDays() * 24 * 60;

                LocalDateTime dateFrom = resolveDateFrom(request);
                LocalDateTime dateTo = resolveDateTo(request);

                List<ManagerDeliveryData> data = slaMetricsRepository.findDeliverySlaByManager(
                                dateFrom,
                                dateTo,
                                request.getDeliveryManagerId(),
                                request.getLeadQualification(),
                                request.getDeliveryService(),
                                sla4Threshold,
                                sla5Threshold,
                                delThreshold);

                return ManagerDeliverySlaResponseDTO.builder()
                                .pipeline("delivery")
                                .period(Period.builder()
                                                .from(dateFrom.toLocalDate().toString())
                                                .to(dateTo.toLocalDate().toString())
                                                .build())
                                .data(data)
                                .build();
        }

        public DeliverySummaryResponseDTO getDeliverySummary(SlaDeliveryRequestDTO request) {
                int sla4Threshold = slaConfig.getToPvzDays() * 24 * 60;
                int sla5Threshold = slaConfig.getPvzStorageDays() * 24 * 60;
                int delThreshold = slaConfig.getDeliveryTotalDays() * 24 * 60;

                LocalDateTime dateFrom = resolveDateFrom(request);
                LocalDateTime dateTo = resolveDateTo(request);

                DeliverySummaryResponseDTO.DeliverySummaryMetrics metrics = slaMetricsRepository.findDeliverySummary(
                                dateFrom,
                                dateTo,
                                request.getDeliveryManagerId(),
                                request.getLeadQualification(),
                                request.getDeliveryService(),
                                sla4Threshold,
                                sla5Threshold,
                                delThreshold);

                return DeliverySummaryResponseDTO.builder()
                                .pipeline("delivery")
                                .period(Period.builder()
                                                .from(dateFrom.toLocalDate().toString())
                                                .to(dateTo.toLocalDate().toString())
                                                .build())
                                .metrics(metrics)
                                .build();
        }

        private LocalDateTime resolveDateFrom(SlaDeliveryRequestDTO request) {
                return request.getDateFrom() != null ? request.getDateFrom() : LocalDateTime.now().minusDays(30);
        }

        private LocalDateTime resolveDateTo(SlaDeliveryRequestDTO request) {
                return request.getDateTo() != null ? request.getDateTo() : LocalDateTime.now();
        }
}