package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.repository.LeadEventRepository;
import com.ck.hackaton.artreid_3.artreid3.repository.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DataLoadControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private LeadEventRepository leadEventRepository;

    @MockitoBean
    private BuildProperties buildProperties;

    @BeforeEach
    void cleanDatabase() {
        leadEventRepository.deleteAll();
        leadRepository.deleteAll();
    }

    @Test
    void load_multipartCsv_importsDataAndIsIdempotent() throws Exception {
        ClassPathResource resource = new ClassPathResource("test-data/dataset-fragment.csv");
        byte[] csvBytes = Files.readAllBytes(resource.getFile().toPath());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "dataset-fragment.csv",
                "text/csv",
                csvBytes
        );

        mockMvc.perform(multipart("/api/data/load").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loaded").value(1))
                .andExpect(jsonPath("$.updated").value(0))
                .andExpect(jsonPath("$.skipped").value(0))
                .andExpect(jsonPath("$.errors").value(0));

        mockMvc.perform(multipart("/api/data/load").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loaded").value(0))
                .andExpect(jsonPath("$.updated").value(0))
                .andExpect(jsonPath("$.skipped").value(1))
                .andExpect(jsonPath("$.errors").value(0));
    }

    @Test
    void load_missingFile_returnsBadRequest() throws Exception {
        mockMvc.perform(multipart("/api/data/load"))
                .andExpect(status().isBadRequest());
    }
}
