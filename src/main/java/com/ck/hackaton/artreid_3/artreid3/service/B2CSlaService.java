package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.model.B2CSummaryDto;
import com.ck.hackaton.artreid_3.artreid3.model.Lead;
import com.ck.hackaton.artreid_3.artreid3.model.LeadEvent;
import com.ck.hackaton.artreid_3.artreid3.repository.LeadEventRepository;
import com.ck.hackaton.artreid_3.artreid3.repository.LeadRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class B2CSlaService {

    private final LeadRepository leadRepository;
    private final LeadEventRepository leadEventRepository;
    private final SlaConfig slaConfig;

    public B2CSlaService(LeadRepository leadRepository,
                         LeadEventRepository leadEventRepository,
                         SlaConfig slaConfig) {
        this.leadRepository = leadRepository;
        this.leadEventRepository = leadEventRepository;
        this.slaConfig = slaConfig;
    }

    public B2CSummaryDto calculateSummary(LocalDate dateFrom, LocalDate dateTo, String managerId, String qualification) {
        List<Lead> allLeads = leadRepository.findAll();
        List<Double> responseMinutes = new ArrayList<>();

        for (Lead lead : allLeads) {
            // Фильтр по менеджеру
            if (managerId != null && !managerId.isEmpty() && !managerId.equals(lead.getManagerId())) {
                continue;
            }

            // Получаем события
            List<LeadEvent> events = leadEventRepository.findByLeadIdAndStageNames(
                    lead.getLeadId(),
                    Arrays.asList("lead_created", "sale")
            );

            LocalDateTime createdTime = null;
            LocalDateTime saleTime = null;

            for (LeadEvent event : events) {
                if ("lead_created".equals(event.getStageName())) {
                    createdTime = event.getEventTime();
                } else if ("sale".equals(event.getStageName())) {
                    saleTime = event.getEventTime();
                }
            }

            if (createdTime != null && saleTime != null) {
                LocalDate eventDate = createdTime.toLocalDate();
                // Фильтр по дате (включительно)
                if (!eventDate.isBefore(dateFrom) && !eventDate.isAfter(dateTo)) {
                    long diffSeconds = java.time.Duration.between(createdTime, saleTime).getSeconds();
                    double minutes = diffSeconds / 60.0;
                    responseMinutes.add(minutes);
                }
            }
        }

        if (responseMinutes.isEmpty()) {
            return createEmptySummary();
        }

        int normative = slaConfig.getFirstResponseNormativeMinutes();

        long withinSla = responseMinutes.stream().filter(m -> m <= normative).count();
        long violatedSla = responseMinutes.size() - withinSla;

        Map<String, Long> breachDistribution = new LinkedHashMap<>();
        long under15 = responseMinutes.stream().filter(m -> m <= 15).count();
        long between15and60 = responseMinutes.stream().filter(m -> m > 15 && m <= 60).count();
        long between1and4hours = responseMinutes.stream().filter(m -> m > 60 && m <= 240).count();
        long over4hours = responseMinutes.stream().filter(m -> m > 240).count();

        breachDistribution.put("≤15 мин", under15);
        breachDistribution.put("15-60 мин", between15and60);
        breachDistribution.put("1-4 ч", between1and4hours);
        breachDistribution.put(">4 ч", over4hours);

        return B2CSummaryDto.builder()
                .totalLeads(responseMinutes.size())
                .withinSlaCount(withinSla)
                .violatedSlaCount(violatedSla)
                .withinSlaPercent((withinSla * 100.0) / responseMinutes.size())
                .violatedSlaPercent((violatedSla * 100.0) / responseMinutes.size())
                .averageFirstResponseMinutes(average(responseMinutes))
                .medianFirstResponseMinutes(median(responseMinutes))
                .percentile90FirstResponseMinutes(percentile(responseMinutes, 90))
                .minMinutes(responseMinutes.stream().min(Double::compare).orElse(0.0))
                .maxMinutes(responseMinutes.stream().max(Double::compare).orElse(0.0))
                .breachDistribution(breachDistribution)
                .managerStats(new HashMap<>())
                .build();
    }

    public B2CSummaryDto calculateSummary(LocalDate dateFrom, LocalDate dateTo, String managerId) {
        return calculateSummary(dateFrom, dateTo, managerId, null);
    }

    private double average(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private double median(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int size = sorted.size();
        if (size % 2 == 0) {
            return (sorted.get(size/2 - 1) + sorted.get(size/2)) / 2.0;
        }
        return sorted.get(size/2);
    }

    private double percentile(List<Double> values, double p) {
        if (values.isEmpty()) return 0.0;
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }

    private B2CSummaryDto createEmptySummary() {
        return B2CSummaryDto.builder()
                .totalLeads(0)
                .withinSlaCount(0)
                .violatedSlaCount(0)
                .withinSlaPercent(0.0)
                .violatedSlaPercent(0.0)
                .averageFirstResponseMinutes(0.0)
                .medianFirstResponseMinutes(0.0)
                .percentile90FirstResponseMinutes(0.0)
                .minMinutes(0.0)
                .maxMinutes(0.0)
                .breachDistribution(new HashMap<>())
                .managerStats(new HashMap<>())
                .build();
    }
}