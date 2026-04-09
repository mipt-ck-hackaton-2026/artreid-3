package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.dto.DeliverySummaryResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeliverySlaService {

    private final EntityManager em;

    public DeliverySummaryResponse getDeliverySummary(LocalDate dateFrom, LocalDate dateTo,
                                                      String managerId, String deliveryService) {

        LocalDateTime fromDateTime = dateFrom.atStartOfDay();
        LocalDateTime toDateTime = dateTo.atTime(23, 59, 59);

        StringBuilder sql = new StringBuilder("""
            WITH delivery_times AS (
                SELECT 
                    l.lead_id,
                    l.manager_id,
                    l.delivery_service,
                    MAX(CASE WHEN le.stage_name = 'HANDED_TO_DELIVERY' THEN le.event_time END) AS handed_ts,
                    MAX(CASE WHEN le.stage_name = 'ISSUED_OR_PVZ' THEN le.event_time END) AS issued_ts,
                    MAX(CASE WHEN le.stage_name = 'RECEIVED' THEN le.event_time END) AS received_ts,
                    MAX(CASE WHEN le.stage_name = 'SALE' THEN le.event_time END) AS sale_ts
                FROM leads l
                JOIN lead_events le ON l.lead_id = le.lead_id
            """);

        boolean hasManager = managerId != null && !managerId.isEmpty();
        boolean hasDelivery = deliveryService != null && !deliveryService.isEmpty();

        if (hasManager || hasDelivery) {
            sql.append(" WHERE ");
            if (hasManager) {
                sql.append(" l.manager_id = :managerId ");
            }
            if (hasManager && hasDelivery) {
                sql.append(" AND ");
            }
            if (hasDelivery) {
                sql.append(" l.delivery_service = :deliveryService ");
            }
        }

        sql.append("""
                GROUP BY l.lead_id, l.manager_id, l.delivery_service
            )
            SELECT 
                COUNT(*) AS total_orders,
                AVG(EXTRACT(EPOCH FROM (issued_ts - handed_ts)) / 86400) AS avg_handed_to_issued,
                AVG(EXTRACT(EPOCH FROM (received_ts - issued_ts)) / 86400) AS avg_issued_to_received,
                COALESCE(SUM(CASE 
                    WHEN (EXTRACT(EPOCH FROM (issued_ts - handed_ts)) / 86400) <= :handedNorm
                     AND (EXTRACT(EPOCH FROM (received_ts - issued_ts)) / 86400) <= :receivedNorm
                    THEN 1 ELSE 0 END), 0) AS compliant_orders
            FROM delivery_times
            WHERE sale_ts BETWEEN :fromDateTime AND :toDateTime
              AND handed_ts IS NOT NULL
              AND issued_ts IS NOT NULL
              AND received_ts IS NOT NULL
        """);

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("fromDateTime", fromDateTime);
        query.setParameter("toDateTime", toDateTime);
        query.setParameter("handedNorm", 5.0);
        query.setParameter("receivedNorm", 2.0);

        if (hasManager) {
            query.setParameter("managerId", managerId);
        }
        if (hasDelivery) {
            query.setParameter("deliveryService", deliveryService);
        }

        Object[] result = (Object[]) query.getSingleResult();
        Long totalOrders = ((Number) result[0]).longValue();
        Double avgHandedToIssued = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
        Double avgIssuedToReceived = result[2] != null ? ((Number) result[2]).doubleValue() : 0.0;
        Long compliantOrders = ((Number) result[3]).longValue(); // теперь не null

        double complianceRate = totalOrders > 0 ? (compliantOrders.doubleValue() / totalOrders) * 100 : 0.0;

        return new DeliverySummaryResponse(
            dateFrom, dateTo,
            avgHandedToIssued,
            avgIssuedToReceived,
            totalOrders.intValue(),
            complianceRate
        );
    }
}