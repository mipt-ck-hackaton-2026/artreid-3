package com.ck.hackaton.artreid_3.artreid3.repository;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.dto.B2CSummaryResponseDTO.B2CMetricDetails;
import com.ck.hackaton.artreid_3.artreid3.dto.B2CSummaryResponseDTO.B2CSummaryMetrics;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerB2CSlaResponseDTO.B2CMetrics;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerB2CSlaResponseDTO.ManagerB2CData;
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
public class B2CMetricsRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SlaConfig slaConfig;

    private static final String RAW_DATA_CTE = """
            WITH raw_data AS (
                SELECT
                    l.manager_id as manager_id,
                    EXTRACT(EPOCH FROM (le_sale.event_time - le_created.event_time)) / 60 AS sla1_interval_min,
                    EXTRACT(EPOCH FROM (COALESCE(le_assembly.event_time, le_handed.event_time) - le_sale.event_time)) / 60 AS sla2_interval_min,
                    EXTRACT(EPOCH FROM (le_handed.event_time - COALESCE(le_assembly.event_time, le_sale.event_time))) / 60 AS sla3_interval_min,
                    EXTRACT(EPOCH FROM (le_handed.event_time - le_created.event_time)) / 60 AS b2c_interval_min
                FROM leads l
                JOIN lead_events le_created ON l.lead_id = le_created.lead_id AND le_created.stage_name = 'CREATED'
                LEFT JOIN lead_events le_sale ON l.lead_id = le_sale.lead_id AND le_sale.stage_name = 'SALE'
                LEFT JOIN lead_events le_assembly ON l.lead_id = le_assembly.lead_id AND le_assembly.stage_name = 'TO_ASSEMBLY'
                LEFT JOIN lead_events le_handed ON l.lead_id = le_handed.lead_id AND le_handed.stage_name = 'HANDED_TO_DELIVERY'
                WHERE l.outcome_unknown = false AND l.lifecycle_incomplete = false
                  AND (le_created.event_time BETWEEN :dateFrom AND :dateTo)
                  AND (:managerId::VARCHAR IS NULL OR l.manager_id = :managerId::VARCHAR)
                  AND (:qualification::VARCHAR IS NULL OR l.lead_qualification = :qualification::VARCHAR)
            )""";

    private String getMetricsColumns() {
        int[] shortBuckets = slaConfig.getBreachBuckets().getShortMinutes();
        int[] daysBuckets = slaConfig.getBreachBuckets().getDays();

        StringBuilder sb = new StringBuilder();
        sb.append(MetricsHelper.buildBaseColumns("sla1", "sla1Threshold"));
        sb.append(MetricsHelper.buildBucketColumns("sla1", "sla1Threshold", shortBuckets, 1));
        sb.append(",\n");

        sb.append(MetricsHelper.buildBaseColumns("sla2", "sla2Threshold"));
        sb.append(MetricsHelper.buildBucketColumns("sla2", "sla2Threshold", daysBuckets, 1440));
        sb.append(",\n");

        sb.append(MetricsHelper.buildBaseColumns("sla3", "sla3Threshold"));
        sb.append(MetricsHelper.buildBucketColumns("sla3", "sla3Threshold", daysBuckets, 1440));
        sb.append(",\n");

        sb.append(MetricsHelper.buildBaseColumns("b2c", "b2cThreshold"));
        sb.append(MetricsHelper.buildBucketColumns("b2c", "b2cThreshold", daysBuckets, 1440));
        
        return sb.toString();
    }

    private B2CMetricDetails mapMetricDetails(java.sql.ResultSet rs, String prefix, int threshold, int[] bounds, String unit) throws java.sql.SQLException {
        long total = rs.getLong(prefix + "_total");
        long met = rs.getLong(prefix + "_met");
        long breach = rs.getLong(prefix + "_breach");

        return B2CMetricDetails.builder()
                .thresholdMinutes(threshold)
                .totalOrders(total)
                .metCount(met)
                .metPercent(MetricsHelper.calculatePercent(met, total))
                .breachCount(breach)
                .breachPercent(MetricsHelper.calculatePercent(breach, total))
                .avgMinutes(MetricsHelper.getDouble(rs.getBigDecimal(prefix + "_avg")))
                .medianMinutes(MetricsHelper.getDouble(rs.getBigDecimal(prefix + "_median")))
                .p90Minutes(MetricsHelper.getDouble(rs.getBigDecimal(prefix + "_p90")))
                .breachDistribution(MetricsHelper.mapBreachDistribution(rs, prefix, bounds, unit))
                .build();
    }

    public B2CSummaryMetrics findB2CSummary(
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String managerId,
            String qualification,
            int sla1ThresholdMinutes,
            int sla2ThresholdMinutes,
            int sla3ThresholdMinutes,
            int b2cThresholdMinutes) {

        String query = RAW_DATA_CTE + " SELECT " + getMetricsColumns() + " FROM raw_data";

        Map<String, Object> params = new HashMap<>();
        params.put("dateFrom", dateFrom);
        params.put("dateTo", dateTo);
        params.put("managerId", managerId);
        params.put("qualification", qualification);
        params.put("sla1Threshold", sla1ThresholdMinutes);
        params.put("sla2Threshold", sla2ThresholdMinutes);
        params.put("sla3Threshold", sla3ThresholdMinutes);
        params.put("b2cThreshold", b2cThresholdMinutes);

        return namedParameterJdbcTemplate.queryForObject(query, params,
                (rs, rowNum) -> B2CSummaryMetrics.builder()
                        .sla1Reaction(mapMetricDetails(rs, "sla1", sla1ThresholdMinutes, slaConfig.getBreachBuckets().getShortMinutes(), "minute"))
                        .sla2ToAssembly(mapMetricDetails(rs, "sla2", sla2ThresholdMinutes, slaConfig.getBreachBuckets().getDays(), "day"))
                        .sla3ToDelivery(mapMetricDetails(rs, "sla3", sla3ThresholdMinutes, slaConfig.getBreachBuckets().getDays(), "day"))
                        .b2cTotal(mapMetricDetails(rs, "b2c", b2cThresholdMinutes, slaConfig.getBreachBuckets().getDays(), "day"))
                        .build());
    }

    public List<ManagerB2CData> findB2CByManager(
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String managerId,
            String qualification,
            int sla1ThresholdMinutes,
            int sla2ThresholdMinutes,
            int sla3ThresholdMinutes,
            int b2cThresholdMinutes) {

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
        params.put("sla1Threshold", sla1ThresholdMinutes);
        params.put("sla2Threshold", sla2ThresholdMinutes);
        params.put("sla3Threshold", sla3ThresholdMinutes);
        params.put("b2cThreshold", b2cThresholdMinutes);

        return namedParameterJdbcTemplate.query(query, params, (rs, rowNum) -> ManagerB2CData.builder()
                .managerId(rs.getString("manager_id"))
                .metrics(B2CMetrics.builder()
                        .sla1Reaction(mapMetricDetails(rs, "sla1", sla1ThresholdMinutes, slaConfig.getBreachBuckets().getShortMinutes(), "minute"))
                        .sla2ToAssembly(mapMetricDetails(rs, "sla2", sla2ThresholdMinutes, slaConfig.getBreachBuckets().getDays(), "day"))
                        .sla3ToDelivery(mapMetricDetails(rs, "sla3", sla3ThresholdMinutes, slaConfig.getBreachBuckets().getDays(), "day"))
                        .b2cTotal(mapMetricDetails(rs, "b2c", b2cThresholdMinutes, slaConfig.getBreachBuckets().getDays(), "day"))
                        .build())
                .build());
    }
}
