package com.ck.hackaton.artreid_3.artreid3.integration;

import com.ck.hackaton.artreid_3.artreid3.model.B2CSummaryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Интеграционные тесты SLA API")
class SlaIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Создаем таблицы
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS leads (
                lead_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                external_lead_id VARCHAR(50),
                manager_id VARCHAR(50),
                lead_created_at BIGINT,
                sale_ts BIGINT,
                lead_responsible_user_id VARCHAR(50)
            )
        """);
        
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS lead_events (
                lead_event_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                lead_id BIGINT NOT NULL,
                stage_name VARCHAR(50) NOT NULL,
                event_time TIMESTAMP
            )
        """);
        
        // Очищаем таблицы
        jdbcTemplate.execute("DELETE FROM lead_events");
        jdbcTemplate.execute("DELETE FROM leads");
        
        // Добавляем тестовые данные
        jdbcTemplate.execute(
            "INSERT INTO leads (lead_id, external_lead_id, manager_id, lead_created_at, sale_ts) VALUES " +
            "(1, 'LEAD_001', 'MANAGER_001', 1740787200, 1740787500)"
        );
        jdbcTemplate.execute(
            "INSERT INTO leads (lead_id, external_lead_id, manager_id, lead_created_at, sale_ts) VALUES " +
            "(2, 'LEAD_002', 'MANAGER_001', 1740787200, 1740788700)"
        );
        jdbcTemplate.execute(
            "INSERT INTO leads (lead_id, external_lead_id, manager_id, lead_created_at, sale_ts) VALUES " +
            "(3, 'LEAD_003', 'MANAGER_002', 1740787200, 1740791700)"
        );
    }

    @Test
    @DisplayName("Health check должен возвращать UP")
    void healthCheckShouldReturnUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/health", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("UP"));
    }

    @Test
    @DisplayName("Config endpoint должен возвращать нормативы")
    void configEndpointShouldReturnNormatives() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/sla/config", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("firstResponseNormativeMinutes"));
    }

    @Test
    @DisplayName("B2C summary endpoint должен возвращать корректную структуру")
    void b2cSummaryShouldReturnCorrectStructure() {
        ResponseEntity<B2CSummaryDto> response = restTemplate.getForEntity(
            "/api/sla/b2c/summary?dateFrom=2026-03-01&dateTo=2026-04-10",
            B2CSummaryDto.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTotalLeads());
    }

    @Test
    @DisplayName("Валидация дат должна возвращать 400")
    void dateValidationShouldReturnBadRequest() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/sla/b2c/summary?dateFrom=2026-04-10&dateTo=2026-04-01",
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("dateFrom must be <= dateTo"));
    }
}
