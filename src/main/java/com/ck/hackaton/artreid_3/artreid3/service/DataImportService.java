package com.ck.hackaton.artreid_3.artreid3.service;

import com.ck.hackaton.artreid_3.artreid3.dto.DataLoadResponseDTO;
import com.ck.hackaton.artreid_3.artreid3.model.CsvLeadRow;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataImportService {

    private final LeadRowProcessor leadRowProcessor;

    public DataLoadResponseDTO loadFromCsv(MultipartFile file) {
        DataLoadResponseDTO response = new DataLoadResponseDTO(0, 0, 0, 0);

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            CsvToBean<CsvLeadRow> csvToBean = new CsvToBeanBuilder<CsvLeadRow>(reader)
                    .withType(CsvLeadRow.class)
                    .build();

            List<CsvLeadRow> batch = new ArrayList<>();
            for (CsvLeadRow row : csvToBean) {
                batch.add(row);
                if (batch.size() >= 1000) {
                    processBatch(batch, response);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                processBatch(batch, response);
            }

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV file: " + file.getOriginalFilename(), e);
        }
    }

    private void processBatch(List<CsvLeadRow> batch, DataLoadResponseDTO response) {
        try {
            LeadRowProcessor.BatchResult result = leadRowProcessor.processBatch(batch);
            response.setLoaded(response.getLoaded() + result.loaded());
            response.setUpdated(response.getUpdated() + result.updated());
            response.setSkipped(response.getSkipped() + result.skipped());
        } catch (Exception e) {
            log.warn("Failed to process CSV batch", e);
            response.setErrors(response.getErrors() + batch.size());
        }
    }
}
