package com.acutus.atk.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class TimeHelper {

    private static ZoneId defaultZoneId = ZoneId.systemDefault();

    public static Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
    }

    public static LocalDate toLocalDate(Date date) {
        return LocalDate.ofInstant(date.toInstant(),defaultZoneId);
    }

}
