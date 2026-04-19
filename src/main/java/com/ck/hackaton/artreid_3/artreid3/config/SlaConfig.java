package com.ck.hackaton.artreid_3.artreid3.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "sla")
public class SlaConfig {

    private B2c b2c = new B2c();
    private Delivery delivery = new Delivery();
    private int fullCycleDays = 16;
    private BreachBuckets breachBuckets = new BreachBuckets();

    @Data
    public static class B2c {
        private int reactionMinutes = 30;
        private int toAssemblyHours = 4;
        private int assemblyToDeliveryDays = 1;
        private int totalDays = 2;
    }

    @Data
    public static class Delivery {
        private int toPvzDays = 5;
        private int pvzStorageDays = 7;
        private int totalDays = 14;
    }

    @Data
    public static class BreachBuckets {
        private int[] shortMinutes = {15, 60};
        private int[] days = {1, 3};
    }

    public int getDeliverySlaThresholdMinutes() {
        return delivery.getTotalDays() * 24 * 60;
    }

    public int getFullCycleThresholdMinutes() {
        return fullCycleDays * 24 * 60;
    }
}