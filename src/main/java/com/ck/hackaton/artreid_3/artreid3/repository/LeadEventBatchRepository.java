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
            INSERT INTO lead_events (lead_id, stage_name, event_time)
            VALUES (?, ?, ?)
            ON CONFLICT (lead_id, stage_name) DO UPDATE SET
                event_time = EXCLUDED.event_time
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
