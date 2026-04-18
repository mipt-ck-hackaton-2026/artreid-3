package com.ck.hackaton.artreid_3.artreid3.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class DateValidationUtil {

    private DateValidationUtil() {
        // Utility class
    }

    public static void validateDateRange(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom must be <= dateTo");
        }
    }

    public static void validateDateRange(LocalDateTime dateFrom, LocalDateTime dateTo) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom must be <= dateTo");
        }
    }
}
