package com.acutus.atk.util;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class TimeHelper {

    public static final DateTimeFormatter yyyyMMddFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static ZoneId defaultZoneId = ZoneId.systemDefault();

    public static Date toDate(LocalDate localDate) {
        return localDate != null ? Date.from(localDate.atStartOfDay(defaultZoneId).toInstant()) : null;
    }

    public static Date toDate(LocalDateTime localDateTime) {
        return localDateTime != null ? Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    public static Timestamp toTimestamp(LocalDateTime localDateTime) {
        return localDateTime != null ? Timestamp.valueOf(localDateTime) : null;
    }

    public static LocalDate toLocalDate(Date date) {
        return LocalDate.ofInstant(date.toInstant(),defaultZoneId);
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(),defaultZoneId);
    }

    public static Timestamp toTimestampFromEpochSecond(int timestamp) {
        return Timestamp.valueOf(LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), defaultZoneId));
    }

}
