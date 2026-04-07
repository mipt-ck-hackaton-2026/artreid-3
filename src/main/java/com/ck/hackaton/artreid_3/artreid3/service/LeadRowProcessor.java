package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.model.CsvLeadRow;
import com.ck.hackaton.artreid_3.artreid3.model.Lead;
import com.ck.hackaton.artreid_3.artreid3.model.LeadEvent;
import com.ck.hackaton.artreid_3.artreid3.model.StageName;
import com.ck.hackaton.artreid_3.artreid3.repository.LeadEventRepository;
import com.ck.hackaton.artreid_3.artreid3.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LeadRowProcessor {

    private final LeadRepository leadRepository;
    private final LeadEventRepository leadEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RowChangeType processRow(CsvLeadRow row) {
        Lead incomingLead = toLead(row);
        UpsertLeadResult leadResult = upsertLead(incomingLead);

        List<LeadEvent> events = toLeadEvents(row, leadResult.lead());
        RowChangeType rowChangeType = leadResult.changeType();

        for (LeadEvent event : events) {
            RowChangeType eventChangeType = upsertEvent(event);
            rowChangeType = mergeRowChangeType(rowChangeType, eventChangeType);
        }

        return rowChangeType;
    }

    private UpsertLeadResult upsertLead(Lead incomingLead) {
        String externalLeadId = incomingLead.getExternalLeadId();
        if (externalLeadId == null) {
            throw new IllegalArgumentException("lead_id is required");
        }

        return leadRepository.findByExternalLeadId(externalLeadId)
                .map(existingLead -> {
                    boolean changed = updateLeadIfChanged(existingLead, incomingLead);

                    if (!changed) {
                        return new UpsertLeadResult(existingLead, RowChangeType.UNCHANGED);
                    }

                    Lead savedLead = leadRepository.save(existingLead);
                    return new UpsertLeadResult(savedLead, RowChangeType.UPDATED);
                })
                .orElseGet(() -> {
                    Lead savedLead = leadRepository.save(incomingLead);
                    return new UpsertLeadResult(savedLead, RowChangeType.LOADED);
                });
    }

    private RowChangeType upsertEvent(LeadEvent incomingEvent) {
        return leadEventRepository.findByLeadAndStageName(incomingEvent.getLead(), incomingEvent.getStageName())
                .map(existingEvent -> {
                    if (Objects.equals(existingEvent.getEventTime(), incomingEvent.getEventTime())) {
                        return RowChangeType.UNCHANGED;
                    }

                    existingEvent.setEventTime(incomingEvent.getEventTime());
                    leadEventRepository.save(existingEvent);
                    return RowChangeType.UPDATED;
                })
                .orElseGet(() -> {
                    leadEventRepository.save(incomingEvent);
                    return RowChangeType.LOADED;
                });
    }

    private boolean updateLeadIfChanged(Lead target, Lead source) {
        boolean changed = false;

        if (!Objects.equals(target.getManagerId(), source.getManagerId())) {
            target.setManagerId(source.getManagerId());
            changed = true;
        }

        if (!Objects.equals(target.getPipelineId(), source.getPipelineId())) {
            target.setPipelineId(source.getPipelineId());
            changed = true;
        }

        if (!Objects.equals(target.getDeliveryService(), source.getDeliveryService())) {
            target.setDeliveryService(source.getDeliveryService());
            changed = true;
        }

        if (!Objects.equals(target.getCity(), source.getCity())) {
            target.setCity(source.getCity());
            changed = true;
        }

        return changed;
    }

    private RowChangeType mergeRowChangeType(RowChangeType rowChangeType, RowChangeType eventChangeType) {
        if (rowChangeType == RowChangeType.LOADED) {
            return RowChangeType.LOADED;
        }

        if (eventChangeType == RowChangeType.UNCHANGED) {
            return rowChangeType;
        }

        return RowChangeType.UPDATED;
    }

    private Lead toLead(CsvLeadRow row) {
        Lead lead = new Lead();
        lead.setExternalLeadId(normalize(row.getLeadId()));
        lead.setManagerId(normalize(row.getManagerId()));
        lead.setPipelineId(row.getPipelineId());
        lead.setDeliveryService(normalize(row.getDeliveryService()));
        lead.setCity(normalize(row.getCity()));
        return lead;
    }

    private List<LeadEvent> toLeadEvents(CsvLeadRow row, Lead lead) {
        List<LeadEvent> events = new ArrayList<>();

        addEventIfPresent(events, lead, StageName.SALE, row.getSaleTs());
        addEventIfPresent(events, lead, StageName.HANDED_TO_DELIVERY, row.getHandedToDeliveryTs());
        addEventIfPresent(events, lead, StageName.ISSUED_OR_PVZ, row.getIssuedOrPvzTs());
        addEventIfPresent(events, lead, StageName.RECEIVED, row.getReceivedTs());
        addEventIfPresent(events, lead, StageName.REJECTED, row.getRejectedTs());
        addEventIfPresent(events, lead, StageName.RETURNED, row.getReturnedTs());

        return events;
    }

    private void addEventIfPresent(List<LeadEvent> events, Lead lead, StageName stageName, String timestampValue) {
        String normalizedTimestamp = normalize(timestampValue);
        if (normalizedTimestamp == null) {
            return;
        }

        LeadEvent event = new LeadEvent();
        event.setLead(lead);
        event.setStageName(stageName);
        event.setEventTime(parseUnixTimestamp(normalizedTimestamp));
        events.add(event);
    }

    private LocalDateTime parseUnixTimestamp(String value) {
        try {
            long epochSeconds = Double.valueOf(value).longValue();
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid timestamp value: " + value, e);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record UpsertLeadResult(Lead lead, RowChangeType changeType) {
    }

    public enum RowChangeType {
        LOADED,
        UPDATED,
        UNCHANGED
    }
}
