package com.insurance.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy/MM/dd");
    
    private static final DateTimeFormatter DATETIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : "";
    }

    public static LocalDate getFiscalYearStart(LocalDate date) {
        int year = date.getYear();
        if (date.getMonthValue() < 4) {
            year--;
        }
        return LocalDate.of(year, 4, 1);
    }

    public static LocalDate getFiscalYearEnd(LocalDate date) {
        return getFiscalYearStart(date).plusYears(1).minusDays(1);
    }

    public static boolean isInFiscalYear(LocalDate date, LocalDate target) {
        LocalDate start = getFiscalYearStart(target);
        LocalDate end = getFiscalYearEnd(target);
        return !date.isBefore(start) && !date.isAfter(end);
    }
}
