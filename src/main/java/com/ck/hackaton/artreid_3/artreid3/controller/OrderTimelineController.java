package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.dto.OrderTimelineResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.service.OrderTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderTimelineController {

    private final OrderTimelineService timelineService;

    @GetMapping("/{leadId}/timeline")
    public OrderTimelineResponseDTO getOrderTimeline(@PathVariable Long leadId) {
        return timelineService.getTimelineResponse(leadId);
    }
}