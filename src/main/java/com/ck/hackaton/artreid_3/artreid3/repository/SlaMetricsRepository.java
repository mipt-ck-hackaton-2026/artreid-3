package com.ck.hackaton.artreid_3.artreid3.repository;

import com.ck.hackaton.artreid_3.artreid3.model.ManagerDeliverySlaMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
                    l.lead_id,
                    l.manager_id,
                    EXTRACT(EPOCH FROM (le_end.event_time - le_start.event_time)) / 60 AS interval_min
                FROM leads l
                JOIN lead_events le_start 
                    ON l.lead_id = le_start.lead_id 
                    AND le_start.stage_name = 'SALE'
                JOIN lead_events le_end 
                    ON l.lead_id = le_end.lead_id 
                    AND le_end.stage_name = 'RECEIVED'
                WHERE le_end.event_time BETWEEN :dateFrom AND :dateTo
                  AND (:managerId::VARCHAR IS NULL OR l.manager_id = :managerId::VARCHAR)
            ),
            metrics AS (
                SELECT 
                    manager_id,
                    COUNT(*) as total_count,
                    AVG(interval_min) as avg_val,
                    percentile_cont(0.5) WITHIN GROUP (ORDER BY interval_min) as median_val,
                    percentile_cont(0.9) WITHIN GROUP (ORDER BY interval_min) as p90_val,
                    COUNT(*) FILTER (WHERE interval_min <= :slaThreshold) as within_sla_count
                FROM raw_data
                GROUP BY manager_id
            )
            SELECT 
                manager_id,
                total_count,
                avg_val,
                median_val,
                p90_val,
                within_sla_count,
                (within_sla_count::NUMERIC / NULLIF(total_count, 0) * 100) as within_sla_percent
            FROM metrics
            ORDER BY manager_id
            """;

    private static final RowMapper<ManagerDeliverySlaMetrics> ROW_MAPPER = (rs, rowNum) ->
            new ManagerDeliverySlaMetrics(
                    rs.getString("manager_id"),
                    rs.getLong("total_count"),
                    rs.getBigDecimal("avg_val"),
                    rs.getBigDecimal("median_val"),
                    rs.getBigDecimal("p90_val"),
                    rs.getLong("within_sla_count"),
                    roundBigDecimal(rs.getBigDecimal("within_sla_percent"))
            );

    private static BigDecimal roundBigDecimal(BigDecimal value) {
        return value != null ? value.setScale(2, RoundingMode.HALF_UP) : null;
    }

    public List<ManagerDeliverySlaMetrics> findDeliverySlaByManager(
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String managerId,
            int slaThresholdMinutes) {

        Map<String, Object> params = new HashMap<>();
        params.put("dateFrom", dateFrom);
        params.put("dateTo", dateTo);
        params.put("managerId", managerId);
        params.put("slaThreshold", slaThresholdMinutes);

        return namedParameterJdbcTemplate.query(DELIVERY_SLA_QUERY, params, ROW_MAPPER);
    }
}