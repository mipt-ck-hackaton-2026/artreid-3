package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.dto.DataLoadResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.service.DataImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataLoadController {

    private final DataImportService dataImportService;

    @PostMapping(value = "/load", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DataLoadResponseDTO load(@RequestParam("file") MultipartFile file) {
        return dataImportService.loadFromCsv(file);
    }
}
