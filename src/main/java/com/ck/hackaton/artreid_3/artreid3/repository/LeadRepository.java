package com.ck.hackaton.artreid_3.artreid3.repository;

import com.ck.hackaton.artreid_3.artreid3.model.Lead;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    Optional<Lead> findByExternalLeadId(String externalLeadId);
    List<Lead> findByExternalLeadIdIn(Collection<String> externalLeadIds);

    @Query("""
        SELECT l FROM Lead l
        WHERE l.managerId = :managerId
        OR (:managerId IS NULL)
        """)
    List<Lead> findLeadsByManager(@Param("managerId") String managerId);

    @Query("SELECT DISTINCT l.managerId FROM Lead l WHERE l.managerId ILIKE CONCAT(:query, '%') ORDER BY l.managerId")
    List<String> findDistinctManagers(@Param("query") String query, Pageable pageable);

    @Query("SELECT DISTINCT l.leadQualification FROM Lead l WHERE l.leadQualification ILIKE CONCAT(:query, '%') ORDER BY l.leadQualification")
    List<String> findDistinctQualifications(@Param("query") String query, Pageable pageable);

    @Query("SELECT DISTINCT l.deliveryService FROM Lead l WHERE l.deliveryService ILIKE CONCAT(:query, '%') ORDER BY l.deliveryService")
    List<String> findDistinctDeliveryServices(@Param("query") String query, Pageable pageable);

    @Query("SELECT DISTINCT l.city FROM Lead l WHERE l.city ILIKE CONCAT(:query, '%') ORDER BY l.city")
    List<String> findDistinctCities(@Param("query") String query, Pageable pageable);

    @Query("SELECT DISTINCT l.deliveryManagerId FROM Lead l WHERE l.deliveryManagerId ILIKE CONCAT(:query, '%') ORDER BY l.deliveryManagerId")
    List<String> findDistinctDeliveryManagers(@Param("query") String query, Pageable pageable);

    @Query("SELECT DISTINCT l.externalLeadId FROM Lead l WHERE l.externalLeadId ILIKE CONCAT(:query, '%') ORDER BY l.externalLeadId")
    List<String> findDistinctExternalLeadIds(@Param("query") String query, Pageable pageable);
}
