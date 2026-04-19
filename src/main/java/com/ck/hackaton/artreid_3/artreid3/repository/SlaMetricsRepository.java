package com.ck.hackaton.artreid_3.artreid3.repository;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.dto.B2CSummaryResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.dto.FullSummaryResponseDTO.FullSummaryMetrics;
import com.ck.hackaton.artreid_3.artreid3.dto.B2CSummaryResponseDTO.B2CMetricDetails;
import com.ck.hackaton.artreid_3.artreid3.dto.ManagerDeliverySlaResponseDTO;
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
    private final SlaConfig slaConfig;

    private static final String RAW_DATA_CTE = """
            WITH raw_data AS (
                SELECT
                    l.manager_id as manager_id,
                    EXTRACT(EPOCH FROM (le_closed.event_time - le_created.event_time)) / 60 AS full_interval_min
                FROM leads l
                JOIN lead_events le_created ON l.lead_id = le_created.lead_id AND le_created.stage_name = 'CREATED'
                LEFT JOIN lead_events le_closed ON l.lead_id = le_closed.lead_id AND le_closed.stage_name = 'CLOSED'
                WHERE l.outcome_unknown = false AND l.lifecycle_incomplete = false
                  AND (le_created.event_time BETWEEN :dateFrom AND :dateTo)
            )""";

    private B2CMetricDetails mapMetricDetails(java.sql.ResultSet rs, String prefix, int threshold, int[] bounds, String unit) throws java.sql.SQLException {
        long total = rs.getLong(prefix + "_total");
        long met = rs.getLong(prefix + "_met");
        long breach = rs.getLong(prefix + "_breach");

        return B2CSummaryResponseDTO.B2CMetricDetails.builder()
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

    public FullSummaryMetrics findFullSummary(
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            int fullThresholdMinutes) {

        String query = RAW_DATA_CTE + " SELECT " + getMetricsColumns() + " FROM raw_data";

        Map<String, Object> params = new HashMap<>();
        params.put("dateFrom", dateFrom);
        params.put("dateTo", dateTo);
        params.put("fullThreshold", fullThresholdMinutes);

        return namedParameterJdbcTemplate.queryForObject(query, params,
                (rs, rowNum) -> FullSummaryMetrics.builder()
                        .fullTotal(mapMetricDetails(rs, "full", fullThresholdMinutes, slaConfig.getBreachBuckets().getDays(), "day"))
                        .build());
    }

    private String getMetricsColumns() {
        int[] daysBuckets = slaConfig.getBreachBuckets().getDays();

        StringBuilder sb = new StringBuilder();
        sb.append(MetricsHelper.buildBaseColumns("full", "fullThreshold"));
        sb.append(MetricsHelper.buildBucketColumns("full", "fullThreshold", daysBuckets, 1440));
        return sb.toString();
    }
}
