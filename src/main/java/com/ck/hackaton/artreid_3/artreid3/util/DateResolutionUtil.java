package com.ck.hackaton.artreid_3.artreid3.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class DateResolutionUtil {

    private DateResolutionUtil() {
        // Utility class
    }

    public static LocalDate[] resolveDateRange(LocalDate dateFrom, LocalDate dateTo) {
        LocalDate from = dateFrom != null ? dateFrom : LocalDate.now().minusDays(30);
        LocalDate to = dateTo != null ? dateTo : LocalDate.now();
        DateValidationUtil.validateDateRange(from, to);
        return new LocalDate[]{from, to};
    }

    public static LocalDateTime[] resolveDateRange(LocalDateTime dateFrom, LocalDateTime dateTo) {
        LocalDateTime from = dateFrom != null ? dateFrom : LocalDateTime.now().minusDays(30);
        LocalDateTime to = dateTo != null ? dateTo : LocalDateTime.now();
        DateValidationUtil.validateDateRange(from, to);
        return new LocalDateTime[]{from, to};
    }
}
