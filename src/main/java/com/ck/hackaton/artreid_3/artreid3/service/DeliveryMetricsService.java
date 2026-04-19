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
import com.ck.hackaton.artreid_3.artreid3.util.DateResolutionUtil;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryMetricsService {

        private final DeliveryMetricsRepository slaMetricsRepository;
        private final SlaConfig slaConfig;

        private record DeliveryThresholds(int sla4, int sla5, int total) {}

        private DeliveryThresholds resolveThresholds() {
                return new DeliveryThresholds(
                        slaConfig.getDelivery().getToPvzDays() * 24 * 60,
                        slaConfig.getDelivery().getPvzStorageDays() * 24 * 60,
                        slaConfig.getDelivery().getTotalDays() * 24 * 60
                );
        }

        public ManagerDeliverySlaResponseDTO getDeliverySlaByManager(SlaDeliveryRequestDTO request) {
                DeliveryThresholds t = resolveThresholds();
                LocalDateTime[] range = DateResolutionUtil.resolveDateRange(request.getDateFrom(), request.getDateTo());
                LocalDateTime dateFrom = range[0];
                LocalDateTime dateTo = range[1];

                List<ManagerDeliveryData> data = slaMetricsRepository.findDeliverySlaByManager(
                                dateFrom,
                                dateTo,
                                request.getDeliveryManagerId(),
                                request.getLeadQualification(),
                                request.getDeliveryService(),
                                t.sla4(),
                                t.sla5(),
                                t.total());

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
                DeliveryThresholds t = resolveThresholds();
                LocalDateTime[] range = DateResolutionUtil.resolveDateRange(request.getDateFrom(), request.getDateTo());
                LocalDateTime dateFrom = range[0];
                LocalDateTime dateTo = range[1];

                DeliverySummaryResponseDTO.DeliverySummaryMetrics metrics = slaMetricsRepository.findDeliverySummary(
                                dateFrom,
                                dateTo,
                                request.getDeliveryManagerId(),
                                request.getLeadQualification(),
                                request.getDeliveryService(),
                                t.sla4(),
                                t.sla5(),
                                t.total());

                return DeliverySummaryResponseDTO.builder()
                                .pipeline("delivery")
                                .period(Period.builder()
                                                .from(dateFrom.toLocalDate().toString())
                                                .to(dateTo.toLocalDate().toString())
                                                .build())
                                .metrics(metrics)
                                .build();
        }

}