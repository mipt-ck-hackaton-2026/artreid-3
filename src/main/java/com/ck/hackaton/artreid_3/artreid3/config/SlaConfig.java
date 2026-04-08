package com.ck.hackaton.artreid_3.artreid3.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SlaConfig {

    private static volatile SlaConfig instance;

    private int reactionMinutes = 30;
    private int toAssemblyHours = 4;
    private int assemblyToDeliveryDays = 1;
    private int b2cTotalDays = 2;

    private int toPvzDays = 5;
    private int pvzStorageDays = 7;
    private int deliveryTotalDays = 14;  // ← используется в задаче #13

    private int fullCycleDays = 16;

    private final int[] shortMinutesBuckets = {15, 60};
    private final int[] daysBuckets = {1, 3};

    public static SlaConfig getInstance() {
        SlaConfig result = instance;
        if (result == null) {
            synchronized (SlaConfig.class) {
                result = instance;
                if (result == null) {
                    instance = result = new SlaConfig();
                }
            }
        }
        return result;
    }

    public int getDeliverySlaThresholdMinutes() {
        return deliveryTotalDays * 24 * 60;
    }

    public int getFullCycleSlaThresholdMinutes() {
        return fullCycleDays * 24 * 60;
    }

}