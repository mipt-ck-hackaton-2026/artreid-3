package com.ck.hackaton.artreid_3.artreid3.repository;

import com.ck.hackaton.artreid_3.artreid3.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    @Query("""
        SELECT l FROM Lead l
        WHERE l.managerId = :managerId
        OR (:managerId IS NULL)
        """)
    List<Lead> findLeadsByManager(@Param("managerId") String managerId);
}