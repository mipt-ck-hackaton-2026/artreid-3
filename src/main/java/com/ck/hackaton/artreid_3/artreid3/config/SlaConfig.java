package com.ck.hackaton.artreid_3.artreid3.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sla.b2c")
public class SlaConfig {
    private int firstResponseNormativeMinutes = 10;

    public int getFirstResponseNormativeMinutes() {
        return firstResponseNormativeMinutes;
    }

    public void setFirstResponseNormativeMinutes(int firstResponseNormativeMinutes) {
        this.firstResponseNormativeMinutes = firstResponseNormativeMinutes;
    }
}