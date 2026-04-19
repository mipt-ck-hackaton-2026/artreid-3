package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.dto.HealthResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final BuildProperties buildProperties;

    @GetMapping
    public ResponseEntity<HealthResponseDTO> health() {
        return ResponseEntity.ok(new HealthResponseDTO("UP", buildProperties.getVersion()));
    }

}
