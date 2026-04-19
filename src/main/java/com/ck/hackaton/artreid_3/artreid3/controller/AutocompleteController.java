package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.service.AutocompleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/autocomplete")
@RequiredArgsConstructor
public class AutocompleteController {

    private final AutocompleteService autocompleteService;

    @GetMapping("/managers")
    public List<String> getManagers(@RequestParam(required = false) String query,
                                   @RequestParam(defaultValue = "10") int limit) {
        return autocompleteService.getManagers(query, limit);
    }

    @GetMapping("/qualifications")
    public List<String> getQualifications(@RequestParam(required = false) String query,
                                         @RequestParam(defaultValue = "10") int limit) {
        return autocompleteService.getQualifications(query, limit);
    }

    @GetMapping("/delivery-services")
    public List<String> getDeliveryServices(@RequestParam(required = false) String query,
                                           @RequestParam(defaultValue = "10") int limit) {
        return autocompleteService.getDeliveryServices(query, limit);
    }

    @GetMapping("/cities")
    public List<String> getCities(@RequestParam(required = false) String query,
                                 @RequestParam(defaultValue = "10") int limit) {
        return autocompleteService.getCities(query, limit);
    }

    @GetMapping("/delivery-managers")
    public List<String> getDeliveryManagers(@RequestParam(required = false) String query,
                                           @RequestParam(defaultValue = "10") int limit) {
        return autocompleteService.getDeliveryManagers(query, limit);
    }

    @GetMapping("/leads")
    public List<String> getLeads(@RequestParam(required = false) String query,
                                @RequestParam(defaultValue = "10") int limit) {
        return autocompleteService.getExternalLeadIds(query, limit);
    }
}
