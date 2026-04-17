package com.ck.hackaton.artreid_3.artreid3.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты конфигурации SLA")
class SlaConfigTest {

    @Test
    @DisplayName("Должен создаваться с значением по умолчанию 30")
    void shouldHaveDefaultValue() {
        SlaConfig slaConfig = new SlaConfig();
        assertNotNull(slaConfig);
        assertEquals(30, slaConfig.getFirstResponseNormativeMinutes(), 
            "Норматив по умолчанию должен быть 30 минут");
    }

    @Test
    @DisplayName("Должен корректно устанавливать новый норматив")
    void shouldSetFirstResponseNormativeMinutes() {
        SlaConfig slaConfig = new SlaConfig();
        slaConfig.setFirstResponseNormativeMinutes(45);
        assertEquals(45, slaConfig.getFirstResponseNormativeMinutes(), 
            "Норматив должен быть 45 минут после установки");
    }

    @Test
    @DisplayName("Должен корректно получать норматив")
    void shouldGetFirstResponseNormativeMinutes() {
        SlaConfig slaConfig = new SlaConfig();
        slaConfig.setFirstResponseNormativeMinutes(60);
        assertEquals(60, slaConfig.getFirstResponseNormativeMinutes(), 
            "Должен вернуть 60 минут");
    }

    @Test
    @DisplayName("Должен допускать отрицательные значения (без валидации)")
    void shouldAllowNegativeValues() {
        SlaConfig slaConfig = new SlaConfig();
        slaConfig.setFirstResponseNormativeMinutes(-10);
        assertEquals(-10, slaConfig.getFirstResponseNormativeMinutes(), 
            "Отрицательные значения допускаются на уровне POJO");
    }
}
