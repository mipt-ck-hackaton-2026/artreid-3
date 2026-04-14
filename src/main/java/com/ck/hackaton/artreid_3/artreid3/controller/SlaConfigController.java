package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.dto.SlaConfigDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sla")
public class SlaConfigController {

    private final SlaConfig slaConfig;

    public SlaConfigController(SlaConfig slaConfig) {
        this.slaConfig = slaConfig;
    }

    @GetMapping("/config")
    public SlaConfigDto getConfig() {
        return new SlaConfigDto(slaConfig.getFirstResponseNormativeMinutes());
    }
}
