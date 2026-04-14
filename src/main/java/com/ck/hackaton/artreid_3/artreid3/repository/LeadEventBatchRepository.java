package com.ck.hackaton.artreid_3.artreid3.repository;

import com.ck.hackaton.artreid_3.artreid3.model.LeadEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LeadEventBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String UPSERT_SQL = """
            MERGE INTO lead_events t
            USING (VALUES (CAST(? AS BIGINT), CAST(? AS VARCHAR), CAST(? AS TIMESTAMP))) s (lead_id, stage_name, event_time)
            ON (t.lead_id = s.lead_id AND t.stage_name = s.stage_name)
            WHEN MATCHED THEN UPDATE SET
                event_time = s.event_time
            WHEN NOT MATCHED THEN INSERT (lead_id, stage_name, event_time)
            VALUES (s.lead_id, s.stage_name, s.event_time)
            """;

    public void batchUpsert(List<LeadEvent> events) {
        if (events.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(UPSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(@org.springframework.lang.NonNull PreparedStatement ps, int i) throws SQLException {
                LeadEvent event = events.get(i);
                ps.setLong(1, event.getLead().getLeadId());
                ps.setString(2, event.getStageName().name());
                ps.setTimestamp(3, Timestamp.valueOf(event.getEventTime()));
            }

            @Override
            public int getBatchSize() {
                return events.size();
            }
        });
    }
}
