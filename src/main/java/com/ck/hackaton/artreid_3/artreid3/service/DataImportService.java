package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.model.CsvLeadRow;
import com.ck.hackaton.artreid_3.artreid3.model.DataLoadResponse;
import com.ck.hackaton.artreid_3.artreid3.model.Lead;
import com.ck.hackaton.artreid_3.artreid3.model.LeadEvent;
import com.ck.hackaton.artreid_3.artreid3.model.StageName;
import com.ck.hackaton.artreid_3.artreid3.repository.LeadEventRepository;
import com.ck.hackaton.artreid_3.artreid3.repository.LeadRepository;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DataImportService {

    private final LeadRepository leadRepository;
    private final LeadEventRepository leadEventRepository;

    public DataLoadResponse loadFromCsv(String filePath) {
        List<CsvLeadRow> rows = readRows(filePath);
        DataLoadResponse response = new DataLoadResponse(0, 0, 0, 0);

        for (CsvLeadRow row : rows) {
            try {
                ChangeType rowChangeType = processRow(row);
                applyStats(response, rowChangeType);
            } catch (Exception e) {
                response.setErrors(response.getErrors() + 1);
            }
        }

        return response;
    }

    private ChangeType processRow(CsvLeadRow row) {
        Lead incomingLead = toLead(row);
        UpsertLeadResult leadResult = upsertLead(incomingLead);

        List<LeadEvent> events = toLeadEvents(row, leadResult.lead());
        ChangeType rowChangeType = leadResult.changeType();

        for (LeadEvent event : events) {
            ChangeType eventChangeType = upsertEvent(event);
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
                        return new UpsertLeadResult(existingLead, ChangeType.UNCHANGED);
                    }

                    Lead savedLead = leadRepository.save(existingLead);
                    return new UpsertLeadResult(savedLead, ChangeType.UPDATED);
                })
                .orElseGet(() -> {
                    Lead savedLead = leadRepository.save(incomingLead);
                    return new UpsertLeadResult(savedLead, ChangeType.LOADED);
                });
    }
    
    private ChangeType upsertEvent(LeadEvent incomingEvent) {
        return leadEventRepository.findByLeadAndStageName(incomingEvent.getLead(), incomingEvent.getStageName())
                .map(existingEvent -> {
                    if (Objects.equals(existingEvent.getEventTime(), incomingEvent.getEventTime())) {
                        return ChangeType.UNCHANGED;
                    }

                    existingEvent.setEventTime(incomingEvent.getEventTime());
                    leadEventRepository.save(existingEvent);
                    return ChangeType.UPDATED;
                })
                .orElseGet(() -> {
                    leadEventRepository.save(incomingEvent);
                    return ChangeType.LOADED;
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

    private ChangeType mergeRowChangeType(ChangeType rowChangeType, ChangeType eventChangeType) {
        if (rowChangeType == ChangeType.LOADED) {
            return ChangeType.LOADED;
        }

        if (eventChangeType == ChangeType.UNCHANGED) {
            return rowChangeType;
        }

        return ChangeType.UPDATED;
    }

    private void applyStats(DataLoadResponse response, ChangeType changeType) {
        switch (changeType) {
            case LOADED -> response.setLoaded(response.getLoaded() + 1);
            case UPDATED -> response.setUpdated(response.getUpdated() + 1);
            case UNCHANGED -> response.setSkipped(response.getSkipped() + 1);
        }
    }

    private List<CsvLeadRow> readRows(String filePath) {
        try (Reader reader = new FileReader(filePath, StandardCharsets.UTF_8)) {
            return new CsvToBeanBuilder<CsvLeadRow>(reader)
                    .withType(CsvLeadRow.class)
                    .build()
                    .parse();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV file: " + filePath, e);
        }
    }

    private Lead toLead(CsvLeadRow row) {
        Lead lead = new Lead();
        lead.setExternalLeadId(normalize(row.getLeadId()));
        lead.setManagerId(normalize(row.getManagerId()));
        lead.setPipelineId(parseInteger(row.getPipelineId()));
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
            long epochSeconds = Long.parseLong(value);
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid timestamp value: " + value, e);
        }
    }

    private Integer parseInteger(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        try {
            return Integer.parseInt(normalizedValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer value: " + value, e);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record UpsertLeadResult(Lead lead, ChangeType changeType) {
    }

    private enum ChangeType {
        LOADED,
        UPDATED,
        UNCHANGED
    }
}
