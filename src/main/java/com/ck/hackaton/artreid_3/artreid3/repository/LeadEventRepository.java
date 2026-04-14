package com.ck.hackaton.artreid_3.artreid3.repository;

import com.ck.hackaton.artreid_3.artreid3.model.Lead;
import com.ck.hackaton.artreid_3.artreid3.model.LeadEvent;
import com.ck.hackaton.artreid_3.artreid3.model.StageName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LeadEventRepository extends JpaRepository<LeadEvent, Long> {
    
    Optional<LeadEvent> findByLeadAndStageName(Lead lead, StageName stageName);
    List<LeadEvent> findByLeadIn(Collection<Lead> leads);

    @Query("""
        SELECT e FROM LeadEvent e 
        WHERE e.lead.leadId = :leadId 
        AND e.stageName IN :stageNames
        ORDER BY e.eventTime
        """)
    List<LeadEvent> findByLeadIdAndStageNames(@Param("leadId") Long leadId,
                                              @Param("stageNames") List<StageName> stageNames);

    @Query("""
        SELECT e1.eventTime, e2.eventTime
        FROM LeadEvent e1
        JOIN LeadEvent e2 ON e1.lead = e2.lead AND e2.stageName = com.ck.hackaton.artreid_3.artreid3.model.StageName.SALE
        JOIN e1.lead l
        WHERE e1.stageName = com.ck.hackaton.artreid_3.artreid3.model.StageName.CREATED
          AND (:managerId IS NULL OR :managerId = '' OR l.managerId = :managerId)
          AND e1.eventTime >= :dateFromStart
          AND e1.eventTime < :dateToNextDay
        """)
    List<Object[]> findResponseTimesByManagerAndDate(
        @Param("managerId") String managerId,
        @Param("dateFromStart") LocalDateTime dateFromStart,
        @Param("dateToNextDay") LocalDateTime dateToNextDay
    );
}
