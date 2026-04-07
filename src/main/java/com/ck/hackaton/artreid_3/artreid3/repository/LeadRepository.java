package com.ck.hackaton.artreid_3.artreid3.repository;

import com.ck.hackaton.artreid_3.artreid3.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
public interface LeadRepository extends JpaRepository<Lead, Long> {
    Optional<Lead> findByExternalLeadId(String externalLeadId);
    List<Lead> findByExternalLeadIdIn(Collection<String> externalLeadIds);
}
