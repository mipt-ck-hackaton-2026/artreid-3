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
            MERGE INTO leads t
            USING (VALUES (CAST(? AS VARCHAR), CAST(? AS VARCHAR), CAST(? AS INTEGER), CAST(? AS VARCHAR), CAST(? AS VARCHAR))) s (external_lead_id, manager_id, pipeline_id, delivery_service, city)
            ON (t.external_lead_id = s.external_lead_id)
            WHEN MATCHED THEN UPDATE SET
                manager_id = s.manager_id,
                pipeline_id = s.pipeline_id,
                delivery_service = s.delivery_service,
                city = s.city
            WHEN NOT MATCHED THEN INSERT (external_lead_id, manager_id, pipeline_id, delivery_service, city)
            VALUES (s.external_lead_id, s.manager_id, s.pipeline_id, s.delivery_service, s.city)
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
