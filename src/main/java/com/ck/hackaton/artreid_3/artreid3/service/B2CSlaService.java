package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.model.B2CSummaryDto;
import com.ck.hackaton.artreid_3.artreid3.model.Lead;
import com.ck.hackaton.artreid_3.artreid3.repository.LeadRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class B2CSlaService {

    private final LeadRepository leadRepository;
    private final SlaConfig slaConfig;

    public B2CSlaService(LeadRepository leadRepository, SlaConfig slaConfig) {
        this.leadRepository = leadRepository;
        this.slaConfig = slaConfig;
    }

    public B2CSummaryDto calculateSummary(LocalDate dateFrom, LocalDate dateTo, String managerId) {
        long fromTs = dateFrom.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        long toTs = dateTo.plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond();

        List<Lead> leads = leadRepository.findLeadsWithFirstResponse(fromTs, toTs, managerId);

        if (leads.isEmpty()) {
            return new B2CSummaryDto(0.0, 0.0, 0L, 0L, 0L);
        }

        List<Double> responseMinutesList = new ArrayList<>();
        int normative = slaConfig.getFirstResponseNormativeMinutes();
        long withinSla = 0L;
        long violatedSla = 0L;

        for (Lead lead : leads) {
            long diffSeconds = lead.getSaleTs() - lead.getLeadCreatedAt();
            double minutes = diffSeconds / 60.0;
            responseMinutesList.add(minutes);

            if (minutes <= normative) {
                withinSla++;
            } else {
                violatedSla++;
            }
        }

        double avg = responseMinutesList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double p90 = percentile(responseMinutesList, 90.0);

        return new B2CSummaryDto(avg, p90, (long) responseMinutesList.size(), withinSla, violatedSla);
    }

    private double percentile(List<Double> values, double percentile) {
        var sorted = values.stream().sorted().toList();
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }
}