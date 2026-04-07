package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.model.CsvLeadRow;
import com.ck.hackaton.artreid_3.artreid3.model.DataLoadResponse;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataImportService {

    private final LeadRowProcessor leadRowProcessor;

    public DataLoadResponse loadFromCsv(MultipartFile file) {
        DataLoadResponse response = new DataLoadResponse(0, 0, 0, 0);

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            CsvToBean<CsvLeadRow> csvToBean = new CsvToBeanBuilder<CsvLeadRow>(reader)
                    .withType(CsvLeadRow.class)
                    .build();

            for (CsvLeadRow row : csvToBean) {
                try {
                    LeadRowProcessor.RowChangeType rowChangeType = leadRowProcessor.processRow(row);
                    applyStats(response, rowChangeType);
                } catch (Exception e) {
                    log.warn("Failed to process CSV row", e);
                    response.setErrors(response.getErrors() + 1);
                }
            }

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV file: " + file.getOriginalFilename(), e);
        }
    }

    private void applyStats(DataLoadResponse response, LeadRowProcessor.RowChangeType changeType) {
        switch (changeType) {
            case LOADED -> response.setLoaded(response.getLoaded() + 1);
            case UPDATED -> response.setUpdated(response.getUpdated() + 1);
            case UNCHANGED -> response.setSkipped(response.getSkipped() + 1);
        }
    }
}
