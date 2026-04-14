package com.ck.hackaton.artreid_3.artreid3.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "sla")
public class SlaConfig {

    private int reactionMinutes = 30;
    private int toAssemblyHours = 4;
    private int assemblyToDeliveryDays = 1;
    private int b2cTotalDays = 2;

    private int toPvzDays = 5;
    private int pvzStorageDays = 7;
    private int deliveryTotalDays = 14;

    private int fullCycleDays = 16;

    private int[] shortMinutesBuckets = {15, 60};
    private int[] daysBuckets = {1, 3};

    private int firstResponseNormativeMinutes = 30;

    public int getDeliverySlaThresholdMinutes() {
        return deliveryTotalDays * 24 * 60;
    }
}