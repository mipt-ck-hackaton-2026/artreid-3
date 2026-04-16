package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.dto.OrderTimelineStepDTO;
import com.ck.hackaton.artreid_3.artreid3.service.OrderTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderTimelineController {

    private final OrderTimelineService timelineService;

    @GetMapping("/{leadId}/timeline")
    public List<OrderTimelineStepDTO> getOrderTimeline(@PathVariable Long leadId) {
        return timelineService.getTimeline(leadId);
    }
}