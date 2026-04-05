package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.model.DataLoadRequest;
import com.ck.hackaton.artreid_3.artreid3.model.DataLoadResponse;
import com.ck.hackaton.artreid_3.artreid3.service.DataImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataLoadController {

    private final DataImportService dataImportService;

    @PostMapping("/load")
    public DataLoadResponse load(@RequestBody DataLoadRequest request) {
        return dataImportService.loadFromCsv(request.getFilePath());
    }
}
