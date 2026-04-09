package com.ck.hackaton.artreid_3.artreid3.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sla.delivery")
public class SlaDeliveryProperties {
    private int handedToPvzDays = 5;
    private int pvzToReceivedDays = 2;

    public int getHandedToPvzDays() { return handedToPvzDays; }
    public void setHandedToPvzDays(int handedToPvzDays) { this.handedToPvzDays = handedToPvzDays; }
    public int getPvzToReceivedDays() { return pvzToReceivedDays; }
    public void setPvzToReceivedDays(int pvzToReceivedDays) { this.pvzToReceivedDays = pvzToReceivedDays; }
}