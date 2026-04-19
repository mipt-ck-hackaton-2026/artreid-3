package com.ck.hackaton.artreid_3.artreid3.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DateValidationUtilTest {

    @Test
    void validateDateRange_bothNull_noException() {
        assertDoesNotThrow(() -> DateValidationUtil.validateDateRange((LocalDate) null, null));
        assertDoesNotThrow(() -> DateValidationUtil.validateDateRange((LocalDateTime) null, null));
    }

    @Test
    void validateDateRange_dateFromBeforeDateTo_noException() {
        assertDoesNotThrow(() -> DateValidationUtil.validateDateRange(LocalDate.now().minusDays(1), LocalDate.now()));
        assertDoesNotThrow(() -> DateValidationUtil.validateDateRange(LocalDateTime.now().minusDays(1), LocalDateTime.now()));
    }

    @Test
    void validateDateRange_dateFromEqualsDateTo_noException() {
        LocalDate date = LocalDate.now();
        assertDoesNotThrow(() -> DateValidationUtil.validateDateRange(date, date));
        
        LocalDateTime dateTime = LocalDateTime.now();
        assertDoesNotThrow(() -> DateValidationUtil.validateDateRange(dateTime, dateTime));
    }

    @Test
    void validateDateRange_dateFromAfterDateTo_throwsIllegalArgument() {
        LocalDate now = LocalDate.now();
        LocalDate past = now.minusDays(1);
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> 
            DateValidationUtil.validateDateRange(now, past)
        );
        assertEquals("dateFrom must be <= dateTo", ex1.getMessage());

        LocalDateTime nowTime = LocalDateTime.now();
        LocalDateTime pastTime = nowTime.minusDays(1);
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> 
            DateValidationUtil.validateDateRange(nowTime, pastTime)
        );
        assertEquals("dateFrom must be <= dateTo", ex2.getMessage());
    }
}
