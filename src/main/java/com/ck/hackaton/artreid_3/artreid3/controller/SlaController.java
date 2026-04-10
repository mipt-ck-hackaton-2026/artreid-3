package com.ck.hackaton.artreid_3.artreid3.controller;

import com.ck.hackaton.artreid_3.artreid3.config.SlaConfig;
import com.ck.hackaton.artreid_3.artreid3.model.B2CSummaryDto;
import com.ck.hackaton.artreid_3.artreid3.model.SlaConfigDto;
import com.ck.hackaton.artreid_3.artreid3.service.B2CSlaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@RestController
@RequestMapping("/api/sla")
public class SlaController {

    private final SlaConfig slaConfig;
    private final B2CSlaService b2CSlaService;

    public SlaController(SlaConfig slaConfig, B2CSlaService b2CSlaService) {
        this.slaConfig = slaConfig;
        this.b2CSlaService = b2CSlaService;
    }

    @GetMapping("/config")
    public SlaConfigDto getConfig() {
        return new SlaConfigDto(slaConfig.getFirstResponseNormativeMinutes());
    }

    @GetMapping("/b2c/summary")
    public B2CSummaryDto getB2CSummary(
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String managerId,
            @RequestParam(required = false) String qualification
    ) {
        if (dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom must be <= dateTo");
        }
        return b2CSlaService.calculateSummary(dateFrom, dateTo, managerId, qualification);
    }
}

/**
 * Глобальный обработчик исключений для контроллеров
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * Обработка ошибки валидации дат
     * Возвращает HTTP 400 BAD_REQUEST вместо 500 INTERNAL_SERVER_ERROR
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of(
                "error", e.getMessage(),
                "status", String.valueOf(HttpStatus.BAD_REQUEST.value())
        );
    }
}