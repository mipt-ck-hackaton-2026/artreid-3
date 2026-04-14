package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.model.ManagerDeliverySlaResponse;
import com.ck.hackaton.artreid_3.artreid3.model.ManagerDeliverySlaResponse.Period;
import com.ck.hackaton.artreid_3.artreid3.model.ManagerDeliverySlaResponse.ManagerDeliveryData;
import com.ck.hackaton.artreid_3.artreid3.model.SlaDeliveryRequest;
import com.ck.hackaton.artreid_3.artreid3.repository.SlaMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlaMetricsService {

    private final SlaMetricsRepository slaMetricsRepository;
    private final SlaConfig slaConfig;

    public ManagerDeliverySlaResponse getDeliverySlaByManager(SlaDeliveryRequest request) {
        int sla4Threshold = slaConfig.getToPvzDays() * 24 * 60;
        int sla5Threshold = slaConfig.getPvzStorageDays() * 24 * 60;
        int delThreshold = slaConfig.getDeliveryTotalDays() * 24 * 60;

        LocalDateTime dateFrom = request.getDateFrom() != null
                ? request.getDateFrom()
                : LocalDateTime.now().minusDays(30);
        LocalDateTime dateTo = request.getDateTo() != null
                ? request.getDateTo()
                : LocalDateTime.now();

        List<ManagerDeliveryData> data = slaMetricsRepository.findDeliverySlaByManager(
                dateFrom,
                dateTo,
                request.getDeliveryManagerId(),
                request.getLeadQualification(),
                request.getDeliveryService(),
                sla4Threshold,
                sla5Threshold,
                delThreshold
        );

        return ManagerDeliverySlaResponse.builder()
                .pipeline("delivery")
                .period(Period.builder()
                        .from(dateFrom.toLocalDate().toString())
                        .to(dateTo.toLocalDate().toString())
                        .build())
                .data(data)
                .build();
    }
}