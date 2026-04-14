package com.ck.hackaton.artreid_3.artreid3.repository;

import com.ck.hackaton.artreid_3.artreid3.model.ManagerDeliverySlaResponse.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class SlaMetricsRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String DELIVERY_SLA_QUERY = """
            WITH raw_data AS (
                SELECT 
                    l.delivery_manager_id as manager_id,
                    EXTRACT(EPOCH FROM (le_pvz.event_time - le_handed.event_time)) / 60 AS sla4_interval_min,
                    EXTRACT(EPOCH FROM (
                        COALESCE(LEAST(le_received.event_time, le_rejected.event_time, le_returned.event_time), 
                                 le_received.event_time, le_rejected.event_time, le_returned.event_time) 
                        - le_pvz.event_time
                    )) / 60 AS sla5_interval_min,
                    EXTRACT(EPOCH FROM (
                        COALESCE(LEAST(le_received.event_time, le_rejected.event_time, le_returned.event_time), 
                                 le_received.event_time, le_rejected.event_time, le_returned.event_time) 
                        - le_handed.event_time
                    )) / 60 AS del_interval_min
                FROM leads l
                LEFT JOIN lead_events le_handed ON l.lead_id = le_handed.lead_id AND le_handed.stage_name = 'HANDED_TO_DELIVERY'
                LEFT JOIN lead_events le_pvz ON l.lead_id = le_pvz.lead_id AND le_pvz.stage_name = 'ISSUED_OR_PVZ'
                LEFT JOIN lead_events le_received ON l.lead_id = le_received.lead_id AND le_received.stage_name = 'RECEIVED'
                LEFT JOIN lead_events le_rejected ON l.lead_id = le_rejected.lead_id AND le_rejected.stage_name = 'REJECTED'
                LEFT JOIN lead_events le_returned ON l.lead_id = le_returned.lead_id AND le_returned.stage_name = 'RETURNED'
                WHERE l.outcome_unknown = false AND l.lifecycle_incomplete = false
                  AND (
                      (le_received.event_time IS NOT NULL AND le_received.event_time BETWEEN :dateFrom AND :dateTo) OR
                      (le_rejected.event_time IS NOT NULL AND le_rejected.event_time BETWEEN :dateFrom AND :dateTo) OR
                      (le_returned.event_time IS NOT NULL AND le_returned.event_time BETWEEN :dateFrom AND :dateTo)
                  )
                  AND (:managerId::VARCHAR IS NULL OR l.delivery_manager_id = :managerId::VARCHAR)
                  AND (:qualification::VARCHAR IS NULL OR l.lead_qualification = :qualification::VARCHAR)
                  AND (:deliveryService::VARCHAR IS NULL OR l.delivery_service = :deliveryService::VARCHAR)
            ),
            metrics AS (
                SELECT 
                    manager_id,
                    
                    COUNT(sla4_interval_min) as sla4_total,
                    COUNT(sla4_interval_min) FILTER (WHERE sla4_interval_min <= :sla4Threshold) as sla4_met,
                    COUNT(sla4_interval_min) FILTER (WHERE sla4_interval_min > :sla4Threshold) as sla4_breach,
                    AVG(sla4_interval_min) as sla4_avg,
                    percentile_cont(0.5) WITHIN GROUP (ORDER BY sla4_interval_min) as sla4_median,
                    percentile_cont(0.9) WITHIN GROUP (ORDER BY sla4_interval_min) as sla4_p90,
                    COUNT(sla4_interval_min) FILTER (WHERE sla4_interval_min > :sla4Threshold AND sla4_interval_min <= :sla4Threshold + 24*60) as sla4_up_to_1d,
                    COUNT(sla4_interval_min) FILTER (WHERE sla4_interval_min > :sla4Threshold + 24*60 AND sla4_interval_min <= :sla4Threshold + 3*24*60) as sla4_1_to_3d,
                    COUNT(sla4_interval_min) FILTER (WHERE sla4_interval_min > :sla4Threshold + 3*24*60) as sla4_over_3d,
            
                    COUNT(sla5_interval_min) as sla5_total,
                    COUNT(sla5_interval_min) FILTER (WHERE sla5_interval_min <= :sla5Threshold) as sla5_met,
                    COUNT(sla5_interval_min) FILTER (WHERE sla5_interval_min > :sla5Threshold) as sla5_breach,
                    AVG(sla5_interval_min) as sla5_avg,
                    percentile_cont(0.5) WITHIN GROUP (ORDER BY sla5_interval_min) as sla5_median,
                    percentile_cont(0.9) WITHIN GROUP (ORDER BY sla5_interval_min) as sla5_p90,
                    COUNT(sla5_interval_min) FILTER (WHERE sla5_interval_min > :sla5Threshold AND sla5_interval_min <= :sla5Threshold + 24*60) as sla5_up_to_1d,
                    COUNT(sla5_interval_min) FILTER (WHERE sla5_interval_min > :sla5Threshold + 24*60 AND sla5_interval_min <= :sla5Threshold + 3*24*60) as sla5_1_to_3d,
                    COUNT(sla5_interval_min) FILTER (WHERE sla5_interval_min > :sla5Threshold + 3*24*60) as sla5_over_3d,
            
                    COUNT(del_interval_min) as del_total,
                    COUNT(del_interval_min) FILTER (WHERE del_interval_min <= :delThreshold) as del_met,
                    COUNT(del_interval_min) FILTER (WHERE del_interval_min > :delThreshold) as del_breach,
                    AVG(del_interval_min) as del_avg,
                    percentile_cont(0.5) WITHIN GROUP (ORDER BY del_interval_min) as del_median,
                    percentile_cont(0.9) WITHIN GROUP (ORDER BY del_interval_min) as del_p90,
                    COUNT(del_interval_min) FILTER (WHERE del_interval_min > :delThreshold AND del_interval_min <= :delThreshold + 24*60) as del_up_to_1d,
                    COUNT(del_interval_min) FILTER (WHERE del_interval_min > :delThreshold + 24*60 AND del_interval_min <= :delThreshold + 3*24*60) as del_1_to_3d,
                    COUNT(del_interval_min) FILTER (WHERE del_interval_min > :delThreshold + 3*24*60) as del_over_3d
                FROM raw_data
                GROUP BY manager_id
            )
            SELECT * FROM metrics ORDER BY manager_id
            """;

    private static double getDouble(BigDecimal value) {
        return value != null ? value.setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0;
    }

    private static double calculatePercent(long part, long total) {
        if (total == 0) return 0.0;
        return BigDecimal.valueOf((double) part / total * 100.0)
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static MetricDetails mapMetricDetails(java.sql.ResultSet rs, String prefix, int threshold) throws java.sql.SQLException {
        long total = rs.getLong(prefix + "_total");
        long met = rs.getLong(prefix + "_met");
        long breach = rs.getLong(prefix + "_breach");
        
        return MetricDetails.builder()
                .thresholdMinutes(threshold)
                .totalOrders(total)
                .metCount(met)
                .metPercent(calculatePercent(met, total))
                .breachCount(breach)
                .breachPercent(calculatePercent(breach, total))
                .avgMinutes(getDouble(rs.getBigDecimal(prefix + "_avg")))
                .medianMinutes(getDouble(rs.getBigDecimal(prefix + "_median")))
                .p90Minutes(getDouble(rs.getBigDecimal(prefix + "_p90")))
                .breachDistribution(BreachDistribution.builder()
                        .upTo1Day(rs.getLong(prefix + "_up_to_1d"))
                        .oneTo3Days(rs.getLong(prefix + "_1_to_3d"))
                        .over3Days(rs.getLong(prefix + "_over_3d"))
                        .build())
                .build();
    }

    public List<ManagerDeliveryData> findDeliverySlaByManager(
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String managerId,
            String qualification,
            String deliveryService,
            int sla4ThresholdMinutes,
            int sla5ThresholdMinutes,
            int delThresholdMinutes) {

        Map<String, Object> params = new HashMap<>();
        params.put("dateFrom", dateFrom);
        params.put("dateTo", dateTo);
        params.put("managerId", managerId);
        params.put("qualification", qualification);
        params.put("deliveryService", deliveryService);
        params.put("sla4Threshold", sla4ThresholdMinutes);
        params.put("sla5Threshold", sla5ThresholdMinutes);
        params.put("delThreshold", delThresholdMinutes);

        return namedParameterJdbcTemplate.query(DELIVERY_SLA_QUERY, params, (rs, rowNum) -> {
            return ManagerDeliveryData.builder()
                    .managerId(rs.getString("manager_id"))
                    .metrics(DeliveryMetrics.builder()
                            .sla4ToPvz(mapMetricDetails(rs, "sla4", sla4ThresholdMinutes))
                            .sla5AtPvz(mapMetricDetails(rs, "sla5", sla5ThresholdMinutes))
                            .deliveryTotal(mapMetricDetails(rs, "del", delThresholdMinutes))
                            .build())
                    .build();
        });
    }
}