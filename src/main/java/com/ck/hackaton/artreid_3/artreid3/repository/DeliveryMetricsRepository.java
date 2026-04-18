package com.ck.hackaton.artreid_3.artreid3.repository;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.dto.DeliverySummaryResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerDeliverySlaResponseDTO.*;

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
public class DeliveryMetricsRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SlaConfig slaConfig;

    private static final String RAW_DATA_CTE = """
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
            )""";

    private String getMetricsColumns() {
        int[] daysBuckets = slaConfig.getBreachBuckets().getDays();

        StringBuilder sb = new StringBuilder();
        sb.append(MetricsHelper.buildBaseColumns("sla4", "sla4Threshold"));
        sb.append(MetricsHelper.buildBucketColumns("sla4", "sla4Threshold", daysBuckets, 1440));
        sb.append(",\n");

        sb.append(MetricsHelper.buildBaseColumns("sla5", "sla5Threshold"));
        sb.append(MetricsHelper.buildBucketColumns("sla5", "sla5Threshold", daysBuckets, 1440));
        sb.append(",\n");

        sb.append(MetricsHelper.buildBaseColumns("del", "delThreshold"));
        sb.append(MetricsHelper.buildBucketColumns("del", "delThreshold", daysBuckets, 1440));
        
        return sb.toString();
    }

    private static double getDouble(BigDecimal value) {
        return value != null ? value.setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0;
    }

    private static double calculatePercent(long part, long total) {
        if (total == 0) return 0.0;
        return BigDecimal.valueOf((double) part / total * 100.0).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private MetricDetails mapMetricDetails(java.sql.ResultSet rs, String prefix, int threshold, int[] bounds) throws java.sql.SQLException {
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
                .breachDistribution(MetricsHelper.mapBreachDistribution(rs, prefix, bounds, "day"))
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

        String query = RAW_DATA_CTE + """
                        , metrics AS (
                            SELECT manager_id,
            """ + getMetricsColumns() + """
                            FROM raw_data GROUP BY manager_id
                        ) SELECT * FROM metrics ORDER BY manager_id """;

        Map<String, Object> params = new HashMap<>();
        params.put("dateFrom", dateFrom);
        params.put("dateTo", dateTo);
        params.put("managerId", managerId);
        params.put("qualification", qualification);
        params.put("deliveryService", deliveryService);
        params.put("sla4Threshold", sla4ThresholdMinutes);
        params.put("sla5Threshold", sla5ThresholdMinutes);
        params.put("delThreshold", delThresholdMinutes);

        return namedParameterJdbcTemplate.query(query, params, (rs, rowNum) -> ManagerDeliveryData.builder()
                .managerId(rs.getString("manager_id"))
                .metrics(DeliveryMetrics.builder()
                        .sla4ToPvz(mapMetricDetails(rs, "sla4", sla4ThresholdMinutes, slaConfig.getBreachBuckets().getDays()))
                        .sla5AtPvz(mapMetricDetails(rs, "sla5", sla5ThresholdMinutes, slaConfig.getBreachBuckets().getDays()))
                        .deliveryTotal(mapMetricDetails(rs, "del", delThresholdMinutes, slaConfig.getBreachBuckets().getDays()))
                        .build())
                .build());
    }

    public DeliverySummaryResponseDTO.DeliverySummaryMetrics findDeliverySummary(
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String managerId,
            String qualification,
            String deliveryService,
            int sla4ThresholdMinutes,
            int sla5ThresholdMinutes,
            int delThresholdMinutes) {

        String query = RAW_DATA_CTE + " SELECT " + getMetricsColumns() + " FROM raw_data";

        Map<String, Object> params = new HashMap<>();
        params.put("dateFrom", dateFrom);
        params.put("dateTo", dateTo);
        params.put("managerId", managerId);
        params.put("qualification", qualification);
        params.put("deliveryService", deliveryService);
        params.put("sla4Threshold", sla4ThresholdMinutes);
        params.put("sla5Threshold", sla5ThresholdMinutes);
        params.put("delThreshold", delThresholdMinutes);

        return namedParameterJdbcTemplate.queryForObject(query, params,
                (rs, rowNum) -> DeliverySummaryResponseDTO.DeliverySummaryMetrics.builder()
                        .sla4ToPvz(mapMetricDetails(rs, "sla4", sla4ThresholdMinutes, slaConfig.getBreachBuckets().getDays()))
                        .sla5AtPvz(mapMetricDetails(rs, "sla5", sla5ThresholdMinutes, slaConfig.getBreachBuckets().getDays()))
                        .deliveryTotal(mapMetricDetails(rs, "del", delThresholdMinutes, slaConfig.getBreachBuckets().getDays()))
                        .build());
    }
}