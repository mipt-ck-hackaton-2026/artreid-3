package com.ck.hackaton.artreid_3.artreid3.repository;

import com.ck.hackaton.artreid_3.artreid3.model.LeadEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LeadEventRepository extends JpaRepository<LeadEvent, Long> {

    @Query("""
        SELECT e FROM LeadEvent e 
        WHERE e.leadId = :leadId 
        AND e.stageName IN :stageNames
        ORDER BY e.eventTime
        """)
    List<LeadEvent> findByLeadIdAndStageNames(@Param("leadId") Long leadId,
                                              @Param("stageNames") List<String> stageNames);
}