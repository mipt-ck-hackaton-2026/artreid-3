package com.ck.hackaton.artreid_3.artreid3.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateResolutionUtilTest {

    @Test
    void resolveDateRange_bothNull_returnsLast30Days() {
        LocalDate[] dates = DateResolutionUtil.resolveDateRange((LocalDate) null, null);
        assertEquals(LocalDate.now().minusDays(30), dates[0]);
        assertEquals(LocalDate.now(), dates[1]);

        LocalDateTime[] dateTimes = DateResolutionUtil.resolveDateRange((LocalDateTime) null, null);
        assertTrue(ChronoUnit.SECONDS.between(LocalDateTime.now().minusDays(30), dateTimes[0]) < 5);
        assertTrue(ChronoUnit.SECONDS.between(LocalDateTime.now(), dateTimes[1]) < 5);
    }

    @Test
    void resolveDateRange_withDates_returnsSameDates() {
        LocalDate from = LocalDate.now().minusDays(5);
        LocalDate to = LocalDate.now().minusDays(1);
        LocalDate[] dates = DateResolutionUtil.resolveDateRange(from, to);
        assertEquals(from, dates[0]);
        assertEquals(to, dates[1]);

        LocalDateTime fromTime = LocalDateTime.now().minusDays(5);
        LocalDateTime toTime = LocalDateTime.now().minusDays(1);
        LocalDateTime[] dateTimes = DateResolutionUtil.resolveDateRange(fromTime, toTime);
        assertEquals(fromTime, dateTimes[0]);
        assertEquals(toTime, dateTimes[1]);
    }

    @Test
    void resolveDateRange_dateFromOnly_returnFromToToday() {
        LocalDate from = LocalDate.now().minusDays(10);
        LocalDate[] dates = DateResolutionUtil.resolveDateRange(from, null);
        assertEquals(from, dates[0]);
        assertEquals(LocalDate.now(), dates[1]);

        LocalDateTime fromTime = LocalDateTime.now().minusDays(10);
        LocalDateTime[] dateTimes = DateResolutionUtil.resolveDateRange(fromTime, null);
        assertEquals(fromTime, dateTimes[0]);
        assertTrue(ChronoUnit.SECONDS.between(LocalDateTime.now(), dateTimes[1]) < 5);
    }
}
