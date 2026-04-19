package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AutocompleteService {

    private final LeadRepository leadRepository;

    public List<String> getManagers(String query, int limit) {
        return leadRepository.findDistinctManagers(normalizeQuery(query), PageRequest.of(0, limit));
    }

    public List<String> getQualifications(String query, int limit) {
        return leadRepository.findDistinctQualifications(normalizeQuery(query), PageRequest.of(0, limit));
    }

    public List<String> getDeliveryServices(String query, int limit) {
        return leadRepository.findDistinctDeliveryServices(normalizeQuery(query), PageRequest.of(0, limit));
    }

    public List<String> getCities(String query, int limit) {
        return leadRepository.findDistinctCities(normalizeQuery(query), PageRequest.of(0, limit));
    }

    public List<String> getDeliveryManagers(String query, int limit) {
        return leadRepository.findDistinctDeliveryManagers(normalizeQuery(query), PageRequest.of(0, limit));
    }

    public List<String> getExternalLeadIds(String query, int limit) {
        return leadRepository.findDistinctExternalLeadIds(normalizeQuery(query), PageRequest.of(0, limit));
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query;
    }
}
