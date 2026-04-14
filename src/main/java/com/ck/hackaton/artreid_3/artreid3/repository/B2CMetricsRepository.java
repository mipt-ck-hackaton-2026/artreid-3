package com.ck.hackaton.artreid_3.artreid3.repository;

import com.ck.hackaton.artreid_3.artreid3.dto.B2CSummaryResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.B2CSummaryResponseDTO.B2CMetricDetails;
import com.ck.hackaton.artreid_3.artreid3.dto.B2CSummaryResponseDTO.B2CSummaryMetrics;
import com.ck.hackaton.artreid_3.artreid3.dto.B2CSummaryResponseDTO.DaysBreachDistribution;
import com.ck.hackaton.artreid_3.artreid3.dto.B2CSummaryResponseDTO.ShortBreachDistribution;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class B2CMetricsRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String RAW_DATA_CTE = """
            WITH raw_data AS (
                SELECT
                    l.manager_id as manager_id,
                    EXTRACT(EPOCH FROM (le_sale.event_time - le_created.event_time)) / 60 AS sla1_interval_min,
                    EXTRACT(EPOCH FROM (le_assembly.event_time - le_sale.event_time)) / 60 AS sla2_interval_min,
                    EXTRACT(EPOCH FROM (le_handed.event_time - le_assembly.event_time)) / 60 AS sla3_interval_min,
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

    private static final String METRICS_COLUMNS = """
                            COUNT(sla1_interval_min) as sla1_total,
                            COUNT(sla1_interval_min) FILTER (WHERE sla1_interval_min <= :sla1Threshold) as sla1_met,
                            COUNT(sla1_interval_min) FILTER (WHERE sla1_interval_min > :sla1Threshold) as sla1_breach,
                            AVG(sla1_interval_min) as sla1_avg,
                            percentile_cont(0.5) WITHIN GROUP (ORDER BY sla1_interval_min) as sla1_median,
                            percentile_cont(0.9) WITHIN GROUP (ORDER BY sla1_interval_min) as sla1_p90,
                            COUNT(sla1_interval_min) FILTER (WHERE sla1_interval_min > :sla1Threshold AND sla1_interval_min <= :sla1Threshold + 15) as sla1_up_to_15m,
                            COUNT(sla1_interval_min) FILTER (WHERE sla1_interval_min > :sla1Threshold + 15 AND sla1_interval_min <= :sla1Threshold + 60) as sla1_15_to_60m,
                            COUNT(sla1_interval_min) FILTER (WHERE sla1_interval_min > :sla1Threshold + 60) as sla1_over_60m,

                            COUNT(sla2_interval_min) as sla2_total,
                            COUNT(sla2_interval_min) FILTER (WHERE sla2_interval_min <= :sla2Threshold) as sla2_met,
                            COUNT(sla2_interval_min) FILTER (WHERE sla2_interval_min > :sla2Threshold) as sla2_breach,
                            AVG(sla2_interval_min) as sla2_avg,
                            percentile_cont(0.5) WITHIN GROUP (ORDER BY sla2_interval_min) as sla2_median,
                            percentile_cont(0.9) WITHIN GROUP (ORDER BY sla2_interval_min) as sla2_p90,
                            COUNT(sla2_interval_min) FILTER (WHERE sla2_interval_min > :sla2Threshold AND sla2_interval_min <= :sla2Threshold + 24*60) as sla2_up_to_1d,
                            COUNT(sla2_interval_min) FILTER (WHERE sla2_interval_min > :sla2Threshold + 24*60 AND sla2_interval_min <= :sla2Threshold + 3*24*60) as sla2_1_to_3d,
                            COUNT(sla2_interval_min) FILTER (WHERE sla2_interval_min > :sla2Threshold + 3*24*60) as sla2_over_3d,

                            COUNT(sla3_interval_min) as sla3_total,
                            COUNT(sla3_interval_min) FILTER (WHERE sla3_interval_min <= :sla3Threshold) as sla3_met,
                            COUNT(sla3_interval_min) FILTER (WHERE sla3_interval_min > :sla3Threshold) as sla3_breach,
                            AVG(sla3_interval_min) as sla3_avg,
                            percentile_cont(0.5) WITHIN GROUP (ORDER BY sla3_interval_min) as sla3_median,
                            percentile_cont(0.9) WITHIN GROUP (ORDER BY sla3_interval_min) as sla3_p90,
                            COUNT(sla3_interval_min) FILTER (WHERE sla3_interval_min > :sla3Threshold AND sla3_interval_min <= :sla3Threshold + 24*60) as sla3_up_to_1d,
                            COUNT(sla3_interval_min) FILTER (WHERE sla3_interval_min > :sla3Threshold + 24*60 AND sla3_interval_min <= :sla3Threshold + 3*24*60) as sla3_1_to_3d,
                            COUNT(sla3_interval_min) FILTER (WHERE sla3_interval_min > :sla3Threshold + 3*24*60) as sla3_over_3d,

                            COUNT(b2c_interval_min) as b2c_total,
                            COUNT(b2c_interval_min) FILTER (WHERE b2c_interval_min <= :b2cThreshold) as b2c_met,
                            COUNT(b2c_interval_min) FILTER (WHERE b2c_interval_min > :b2cThreshold) as b2c_breach,
                            AVG(b2c_interval_min) as b2c_avg,
                            percentile_cont(0.5) WITHIN GROUP (ORDER BY b2c_interval_min) as b2c_median,
                            percentile_cont(0.9) WITHIN GROUP (ORDER BY b2c_interval_min) as b2c_p90,
                            COUNT(b2c_interval_min) FILTER (WHERE b2c_interval_min > :b2cThreshold AND b2c_interval_min <= :b2cThreshold + 24*60) as b2c_up_to_1d,
                            COUNT(b2c_interval_min) FILTER (WHERE b2c_interval_min > :b2cThreshold + 24*60 AND b2c_interval_min <= :b2cThreshold + 3*24*60) as b2c_1_to_3d,
                            COUNT(b2c_interval_min) FILTER (WHERE b2c_interval_min > :b2cThreshold + 3*24*60) as b2c_over_3d
            """;

    private static final String B2C_SUMMARY_QUERY = RAW_DATA_CTE + """
                        SELECT
            """ + METRICS_COLUMNS + """
            FROM raw_data
            """;

    private static double getDouble(BigDecimal value) {
        return value != null ? value.setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0;
    }

    private static double calculatePercent(long part, long total) {
        if (total == 0)
            return 0.0;
        return BigDecimal.valueOf((double) part / total * 100.0)
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static B2CMetricDetails mapMetricDetailsShort(java.sql.ResultSet rs, String prefix, int threshold)
            throws java.sql.SQLException {
        long total = rs.getLong(prefix + "_total");
        long met = rs.getLong(prefix + "_met");
        long breach = rs.getLong(prefix + "_breach");

        return B2CMetricDetails.builder()
                .thresholdMinutes(threshold)
                .totalOrders(total)
                .metCount(met)
                .metPercent(calculatePercent(met, total))
                .breachCount(breach)
                .breachPercent(calculatePercent(breach, total))
                .avgMinutes(getDouble(rs.getBigDecimal(prefix + "_avg")))
                .medianMinutes(getDouble(rs.getBigDecimal(prefix + "_median")))
                .p90Minutes(getDouble(rs.getBigDecimal(prefix + "_p90")))
                .breachDistribution(ShortBreachDistribution.builder()
                        .upTo15Min(rs.getLong(prefix + "_up_to_15m"))
                        .fifteenTo60Min(rs.getLong(prefix + "_15_to_60m"))
                        .over60Min(rs.getLong(prefix + "_over_60m"))
                        .build())
                .build();
    }

    private static B2CMetricDetails mapMetricDetailsDays(java.sql.ResultSet rs, String prefix, int threshold)
            throws java.sql.SQLException {
        long total = rs.getLong(prefix + "_total");
        long met = rs.getLong(prefix + "_met");
        long breach = rs.getLong(prefix + "_breach");

        return B2CMetricDetails.builder()
                .thresholdMinutes(threshold)
                .totalOrders(total)
                .metCount(met)
                .metPercent(calculatePercent(met, total))
                .breachCount(breach)
                .breachPercent(calculatePercent(breach, total))
                .avgMinutes(getDouble(rs.getBigDecimal(prefix + "_avg")))
                .medianMinutes(getDouble(rs.getBigDecimal(prefix + "_median")))
                .p90Minutes(getDouble(rs.getBigDecimal(prefix + "_p90")))
                .breachDistribution(DaysBreachDistribution.builder()
                        .upTo1Day(rs.getLong(prefix + "_up_to_1d"))
                        .oneTo3Days(rs.getLong(prefix + "_1_to_3d"))
                        .over3Days(rs.getLong(prefix + "_over_3d"))
                        .build())
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

        Map<String, Object> params = new HashMap<>();
        params.put("dateFrom", dateFrom);
        params.put("dateTo", dateTo);
        params.put("managerId", managerId);
        params.put("qualification", qualification);
        params.put("sla1Threshold", sla1ThresholdMinutes);
        params.put("sla2Threshold", sla2ThresholdMinutes);
        params.put("sla3Threshold", sla3ThresholdMinutes);
        params.put("b2cThreshold", b2cThresholdMinutes);

        return namedParameterJdbcTemplate.queryForObject(B2C_SUMMARY_QUERY, params,
                (rs, rowNum) -> B2CSummaryMetrics.builder()
                        .sla1Reaction(mapMetricDetailsShort(rs, "sla1", sla1ThresholdMinutes))
                        .sla2ToAssembly(mapMetricDetailsDays(rs, "sla2", sla2ThresholdMinutes))
                        .sla3ToDelivery(mapMetricDetailsDays(rs, "sla3", sla3ThresholdMinutes))
                        .b2cTotal(mapMetricDetailsDays(rs, "b2c", b2cThresholdMinutes))
                        .build());
    }
}