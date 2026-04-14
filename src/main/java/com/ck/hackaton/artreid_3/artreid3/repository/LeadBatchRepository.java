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
            USING (VALUES (CAST(? AS VARCHAR), CAST(? AS VARCHAR), CAST(? AS INTEGER), CAST(? AS VARCHAR), CAST(? AS VARCHAR),
                           CAST(? AS VARCHAR), CAST(? AS VARCHAR), CAST(? AS BOOLEAN), CAST(? AS BOOLEAN)))
                s (external_lead_id, manager_id, pipeline_id, delivery_service, city,
                   delivery_manager_id, lead_qualification, outcome_unknown, lifecycle_incomplete)
            ON (t.external_lead_id = s.external_lead_id)
            WHEN MATCHED THEN UPDATE SET
                manager_id = s.manager_id,
                pipeline_id = s.pipeline_id,
                delivery_service = s.delivery_service,
                city = s.city,
                delivery_manager_id = s.delivery_manager_id,
                lead_qualification = s.lead_qualification,
                outcome_unknown = s.outcome_unknown,
                lifecycle_incomplete = s.lifecycle_incomplete
            WHEN NOT MATCHED THEN INSERT (external_lead_id, manager_id, pipeline_id, delivery_service, city,
                                          delivery_manager_id, lead_qualification, outcome_unknown, lifecycle_incomplete)
            VALUES (s.external_lead_id, s.manager_id, s.pipeline_id, s.delivery_service, s.city,
                    s.delivery_manager_id, s.lead_qualification, s.outcome_unknown, s.lifecycle_incomplete)
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
                ps.setString(6, lead.getDeliveryManagerId());
                ps.setString(7, lead.getLeadQualification());
                if (lead.getOutcomeUnknown() != null) {
                    ps.setBoolean(8, lead.getOutcomeUnknown());
                } else {
                    ps.setBoolean(8, false);
                }
                if (lead.getLifecycleIncomplete() != null) {
                    ps.setBoolean(9, lead.getLifecycleIncomplete());
                } else {
                    ps.setBoolean(9, false);
                }
            }

            @Override
            public int getBatchSize() {
                return leads.size();
            }
        });
    }
}
