package com.ck.hackaton.artreid_3.artreid3.repository;

import com.ck.hackaton.artreid_3.artreid3.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, String> { // ← PK = String (leadId)

    @Query("""
        SELECT l FROM Lead l
        WHERE l.leadCreatedAt >= :fromTs
          AND l.leadCreatedAt < :toTs
          AND (:managerId IS NULL OR l.leadResponsibleUserId = :managerId)
          AND l.saleTs IS NOT NULL
          AND l.leadCreatedAt IS NOT NULL
        """)
    List<Lead> findLeadsWithFirstResponse(
            @Param("fromTs") Long fromTs,
            @Param("toTs") Long toTs,
            @Param("managerId") String managerId
    );
}