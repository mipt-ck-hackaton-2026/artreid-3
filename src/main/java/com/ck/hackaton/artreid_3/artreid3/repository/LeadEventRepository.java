package com.ck.hackaton.artreid_3.artreid3.repository;

import com.ck.hackaton.artreid_3.artreid3.model.Lead;
import com.ck.hackaton.artreid_3.artreid3.model.LeadEvent;
import com.ck.hackaton.artreid_3.artreid3.model.StageName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LeadEventRepository extends JpaRepository<LeadEvent, Long> {
    Optional<LeadEvent> findByLeadAndStageName(Lead lead, StageName stageName);
    List<LeadEvent> findByLeadIn(Collection<Lead> leads);
}
