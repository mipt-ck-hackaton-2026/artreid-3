package com.ck.hackaton.artreid_3.artreid3.repository;

import com.ck.hackaton.artreid_3.artreid3.model.Lead;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LeadBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String UPSERT_SQL = """
            INSERT INTO leads (external_lead_id, manager_id, pipeline_id, delivery_service, city)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (external_lead_id) DO UPDATE SET
                manager_id = EXCLUDED.manager_id,
                pipeline_id = EXCLUDED.pipeline_id,
                delivery_service = EXCLUDED.delivery_service,
                city = EXCLUDED.city
            """;

    public void batchUpsert(List<Lead> leads) {
        if (leads.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(UPSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(@org.springframework.lang.NonNull PreparedStatement ps, int i) throws SQLException {
                Lead lead = leads.get(i);
                ps.setString(1, lead.getExternalLeadId());
                ps.setString(2, lead.getManagerId());
                if (lead.getPipelineId() != null) {
                    ps.setInt(3, lead.getPipelineId());
                } else {
                    ps.setNull(3, Types.INTEGER);
                }
                ps.setString(4, lead.getDeliveryService());
                ps.setString(5, lead.getCity());
            }

            @Override
            public int getBatchSize() {
                return leads.size();
            }
        });
    }
}
