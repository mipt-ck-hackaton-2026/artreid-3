package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.model.ManagerDeliverySlaMetrics;
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

    public List<ManagerDeliverySlaMetrics> getDeliverySlaByManager(SlaDeliveryRequest request) {
        LocalDateTime dateFrom = request.getDateFrom() != null
                ? request.getDateFrom()
                : LocalDateTime.now().minusDays(30);
        LocalDateTime dateTo = request.getDateTo() != null
                ? request.getDateTo()
                : LocalDateTime.now();

        return slaMetricsRepository.findDeliverySlaByManager(
                dateFrom,
                dateTo,
                request.getManagerId()
        );
    }
}