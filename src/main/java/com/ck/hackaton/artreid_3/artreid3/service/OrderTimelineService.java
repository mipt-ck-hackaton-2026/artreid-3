package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.config.SlaDeliveryProperties;
import com.ck.hackaton.artreid_3.artreid3.dto.OrderTimelineResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.OrderTimelineStepDTO;
import com.ck.hackaton.artreid_3.artreid3.model.LeadEvent;
import com.ck.hackaton.artreid_3.artreid3.model.StageName;
import com.ck.hackaton.artreid_3.artreid3.repository.LeadEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderTimelineService {

    private final LeadEventRepository eventRepository;
    private final SlaConfig slaConfig;
    private final SlaDeliveryProperties deliveryProps;

    public OrderTimelineResponseDTO getTimelineResponse(Long leadId) {
        List<LeadEvent> events = eventRepository.findByLeadIdAndStageNames(leadId, List.of(StageName.values()));

        if (events.isEmpty()) {
            return OrderTimelineResponseDTO.builder()
                    .pipeline("delivery")
                    .data(List.of())
                    .build();
        }

        events.sort(Comparator.comparing(LeadEvent::getEventTime));

        List<OrderTimelineStepDTO> steps = new ArrayList<>();
        for (int i = 0; i < events.size() - 1; i++) {
            LeadEvent current = events.get(i);
            LeadEvent next = events.get(i + 1);

            long minutes = Duration.between(current.getEventTime(), next.getEventTime()).toMinutes();
            boolean violated = isSlaViolated(current.getStageName(), next.getStageName(), minutes);

            steps.add(OrderTimelineStepDTO.builder()
                    .stage(current.getStageName())
                    .startTime(current.getEventTime())
                    .endTime(next.getEventTime())
                    .durationMinutes(minutes)
                    .durationDays(minutes / 1440.0)
                    .slaViolated(violated)
                    .build());
        }

        return OrderTimelineResponseDTO.builder()
                .period(OrderTimelineResponseDTO.PeriodDto.builder()
                        .from(events.get(0).getEventTime().toString())
                        .to(events.get(events.size() - 1).getEventTime().toString())
                        .build())
                .pipeline("lead")
                .data(steps)
                .build();
    }

    public List<OrderTimelineStepDTO> getTimeline(Long leadId) {
        List<LeadEvent> events = eventRepository.findByLeadIdAndStageNames(leadId, List.of(StageName.values()));
        events.sort(Comparator.comparing(LeadEvent::getEventTime));

        List<OrderTimelineStepDTO> timeline = new ArrayList<>();

        for (int i = 0; i < events.size() - 1; i++) {
            LeadEvent current = events.get(i);
            LeadEvent next = events.get(i + 1);

            long minutes = Duration.between(current.getEventTime(), next.getEventTime()).toMinutes();
            boolean violated = isSlaViolated(current.getStageName(), next.getStageName(), minutes);

            timeline.add(OrderTimelineStepDTO.builder()
                    .stage(current.getStageName())
                    .startTime(current.getEventTime())
                    .endTime(next.getEventTime())
                    .durationMinutes(minutes)
                    .durationDays(minutes / 1440.0)
                    .slaViolated(violated)
                    .build());
        }

        return timeline;
    }

    private boolean isSlaViolated(StageName from, StageName to, long actualMinutes) {
        if (from == StageName.CREATED && to == StageName.SALE)
            return actualMinutes > slaConfig.getReactionMinutes();

        if (from == StageName.SALE && to == StageName.TO_ASSEMBLY)
            return actualMinutes > (slaConfig.getToAssemblyHours() * 60);

        if (from == StageName.TO_ASSEMBLY && to == StageName.HANDED_TO_DELIVERY)
            return actualMinutes > (slaConfig.getAssemblyToDeliveryDays() * 1440);

        if (from == StageName.HANDED_TO_DELIVERY && to == StageName.ISSUED_OR_PVZ)
            return actualMinutes > (deliveryProps.getHandedToPvzDays() * 1440);

        if (from == StageName.ISSUED_OR_PVZ && to == StageName.RECEIVED)
            return actualMinutes > (deliveryProps.getPvzToReceivedDays() * 1440);

        return false;
    }
}