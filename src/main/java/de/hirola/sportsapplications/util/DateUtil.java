package de.hirola.sportsapplications.util;

import javax.validation.constraints.Null;
import java.time.*;
import java.util.Date;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Util class to convert dates.
 * The actual database do not support add as LocalDate.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 *
 */
public final class DateUtil {

    @Null
    public static Date getDateFromLocalDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Null
    public static LocalDate getLocalDateFromDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date getDateFromNow() {
        return Date.from(Instant.now());
    }

    public static LocalDate getLocalDateFromNow() {
        return LocalDate.from(Instant.now().atZone(ZoneId.systemDefault()));
    }

    public static long getTimeStampFromNow() {
        return LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static LocalDate getMondayOfActualWeek() {
        LocalDate today = DateUtil.getLocalDateFromNow();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        if (dayOfWeek != DayOfWeek.MONDAY) {
            // from Tuesday the start date is next Monday
            long daysToAdd = 8 - dayOfWeek.getValue();
            return today.plusDays(daysToAdd);
        }
        return today;
    }
}
