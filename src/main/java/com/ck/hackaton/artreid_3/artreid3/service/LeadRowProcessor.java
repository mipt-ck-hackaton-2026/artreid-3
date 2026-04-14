package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.model.CsvLeadRow;
import com.ck.hackaton.artreid_3.artreid3.model.Lead;
import com.ck.hackaton.artreid_3.artreid3.model.LeadEvent;
import com.ck.hackaton.artreid_3.artreid3.model.StageName;
import com.ck.hackaton.artreid_3.artreid3.repository.LeadBatchRepository;
import com.ck.hackaton.artreid_3.artreid3.repository.LeadEventBatchRepository;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadRowProcessor {

    private final LeadRepository leadRepository;
    private final LeadEventRepository leadEventRepository;
    private final LeadBatchRepository leadBatchRepository;
    private final LeadEventBatchRepository leadEventBatchRepository;

    public record BatchResult(int loaded, int updated, int skipped) {}

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchResult processBatch(List<CsvLeadRow> rows) {
        if (rows.isEmpty()) {
            return new BatchResult(0, 0, 0);
        }

        Map<String, CsvLeadRow> rowMap = deduplicateRows(rows);
        List<CsvLeadRow> uniqueRows = new ArrayList<>(rowMap.values());
        List<String> externalLeadIds = new ArrayList<>(rowMap.keySet());

        Map<String, RowChangeType> rowTypeMap = upsertLeads(uniqueRows, externalLeadIds);

        Map<String, Lead> allLeadsAfterUpsert = fetchLeadsByExternalIds(externalLeadIds);

        processEvents(uniqueRows, allLeadsAfterUpsert, rowTypeMap);

        return computeStats(rows, rowMap, rowTypeMap);
    }

    private Map<String, CsvLeadRow> deduplicateRows(List<CsvLeadRow> rows) {
        Map<String, CsvLeadRow> rowMap = new LinkedHashMap<>();
        for (CsvLeadRow row : rows) {
            String leadId = normalize(row.getLeadId());
            if (leadId != null) {
                rowMap.put(leadId, row);
            }
        }
        return rowMap;
    }

    private Map<String, RowChangeType> upsertLeads(List<CsvLeadRow> uniqueRows, List<String> externalLeadIds) {
        Map<String, Lead> existingLeads = leadRepository.findByExternalLeadIdIn(externalLeadIds)
                .stream()
                .collect(Collectors.toMap(Lead::getExternalLeadId, Function.identity()));

        List<Lead> leadsToUpsert = new ArrayList<>();
        Map<String, RowChangeType> rowTypeMap = new HashMap<>();

        for (CsvLeadRow row : uniqueRows) {
            String extId = normalize(row.getLeadId());
            Lead incoming = toLead(row);
            Lead existing = existingLeads.get(extId);

            if (existing == null) {
                rowTypeMap.put(extId, RowChangeType.LOADED);
                leadsToUpsert.add(incoming);
            } else {
                if (updateLeadIfChanged(existing, incoming)) {
                    rowTypeMap.put(extId, RowChangeType.UPDATED);
                    leadsToUpsert.add(existing);
                } else {
                    rowTypeMap.put(extId, RowChangeType.UNCHANGED);
                }
            }
        }

        leadBatchRepository.batchUpsert(leadsToUpsert);
        return rowTypeMap;
    }

    private Map<String, Lead> fetchLeadsByExternalIds(List<String> externalLeadIds) {
        return leadRepository.findByExternalLeadIdIn(externalLeadIds)
                .stream()
                .collect(Collectors.toMap(Lead::getExternalLeadId, Function.identity()));
    }

    private void processEvents(List<CsvLeadRow> uniqueRows,
                               Map<String, Lead> allLeadsAfterUpsert,
                               Map<String, RowChangeType> rowTypeMap) {
        Map<Long, List<LeadEvent>> existingEventsByLead = leadEventRepository
                .findByLeadIn(allLeadsAfterUpsert.values())
                .stream()
                .collect(Collectors.groupingBy(e -> e.getLead().getLeadId()));

        List<LeadEvent> eventsToUpsert = new ArrayList<>();

        for (CsvLeadRow row : uniqueRows) {
            String extId = normalize(row.getLeadId());
            Lead leadEntity = allLeadsAfterUpsert.get(extId);
            RowChangeType currentRowType = rowTypeMap.get(extId);

            if (leadEntity != null) {
                List<LeadEvent> incomingEvents = toLeadEvents(row, leadEntity);
                List<LeadEvent> exEvents = existingEventsByLead
                        .getOrDefault(leadEntity.getLeadId(), Collections.emptyList());

                for (LeadEvent incEv : incomingEvents) {
                    RowChangeType evType = classifyEvent(incEv, exEvents);

                    if (evType != RowChangeType.UNCHANGED) {
                        eventsToUpsert.add(incEv);
                    }

                    currentRowType = mergeRowChangeType(currentRowType, evType);
                }
            }
            rowTypeMap.put(extId, currentRowType);
        }

        leadEventBatchRepository.batchUpsert(eventsToUpsert);
    }

    private RowChangeType classifyEvent(LeadEvent incoming, List<LeadEvent> existingEvents) {
        LeadEvent existing = existingEvents.stream()
                .filter(e -> e.getStageName() == incoming.getStageName())
                .findFirst()
                .orElse(null);

        if (existing == null) {
            return RowChangeType.LOADED;
        }
        if (!Objects.equals(existing.getEventTime(), incoming.getEventTime())) {
            return RowChangeType.UPDATED;
        }
        return RowChangeType.UNCHANGED;
    }

    private BatchResult computeStats(List<CsvLeadRow> rows,
                                     Map<String, CsvLeadRow> rowMap,
                                     Map<String, RowChangeType> rowTypeMap) {
        int totalLoaded = 0;
        int totalUpdated = 0;
        int totalSkipped = 0;

        for (CsvLeadRow row : rows) {
            String extId = normalize(row.getLeadId());
            if (extId == null || row != rowMap.get(extId)) {
                totalSkipped++;
            } else {
                RowChangeType type = rowTypeMap.get(extId);
                if (type == RowChangeType.LOADED) totalLoaded++;
                else if (type == RowChangeType.UPDATED) totalUpdated++;
                else totalSkipped++;
            }
        }

        return new BatchResult(totalLoaded, totalUpdated, totalSkipped);
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
        if (!Objects.equals(target.getDeliveryManagerId(), source.getDeliveryManagerId())) {
            target.setDeliveryManagerId(source.getDeliveryManagerId());
            changed = true;
        }
        if (!Objects.equals(target.getLeadQualification(), source.getLeadQualification())) {
            target.setLeadQualification(source.getLeadQualification());
            changed = true;
        }
        if (!Objects.equals(target.getOutcomeUnknown(), source.getOutcomeUnknown())) {
            target.setOutcomeUnknown(source.getOutcomeUnknown());
            changed = true;
        }
        if (!Objects.equals(target.getLifecycleIncomplete(), source.getLifecycleIncomplete())) {
            target.setLifecycleIncomplete(source.getLifecycleIncomplete());
            changed = true;
        }

        return changed;
    }

    private Lead toLead(CsvLeadRow row) {
        Lead lead = new Lead();
        lead.setExternalLeadId(normalize(row.getLeadId()));
        lead.setManagerId(normalize(row.getManagerId()));
        lead.setPipelineId(row.getPipelineId());
        lead.setDeliveryService(normalize(row.getDeliveryService()));
        lead.setCity(normalize(row.getCity()));
        lead.setDeliveryManagerId(normalize(row.getDeliveryManagerId()));
        lead.setLeadQualification(normalize(row.getLeadQualification()));
        lead.setOutcomeUnknown(parseCsvBool(row.getOutcomeUnknown()));
        lead.setLifecycleIncomplete(parseCsvBool(row.getLifecycleIncomplete()));
        return lead;
    }

    private List<LeadEvent> toLeadEvents(CsvLeadRow row, Lead lead) {
        List<LeadEvent> events = new ArrayList<>();

        addEventIfPresent(events, lead, StageName.CREATED, row.getLeadCreatedAt());
        addEventIfPresent(events, lead, StageName.CLOSED, row.getClosedTs());
        addEventIfPresent(events, lead, StageName.TO_ASSEMBLY, row.getLeadDateToAssembly());
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

    private Boolean parseCsvBool(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return false;
        }
        return "1".equals(normalized) || "true".equalsIgnoreCase(normalized);
    }

    public enum RowChangeType {
        LOADED,
        UPDATED,
        UNCHANGED
    }
}
