package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.dto.OrderTimelineResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.service.OrderTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderTimelineController {

    private final OrderTimelineService timelineService;

    @GetMapping("/{leadId}/timeline")
    public ResponseEntity<OrderTimelineResponseDTO> getOrderTimeline(@PathVariable Long leadId) {
        return ResponseEntity.ok(timelineService.getTimelineResponse(leadId));
    }
}