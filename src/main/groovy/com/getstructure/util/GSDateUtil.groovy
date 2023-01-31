package com.getstructure.util

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class GSDateUtil {
    static final SimpleDateFormat iso8601DateFormatter = new SimpleDateFormat('yyyy-MM-dd')
    static final SimpleDateFormat iso8601DateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    static final DateTimeFormatter iso860DBDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    static Date parse( String date ) {
        return date ? iso8601DateFormatter.parse( date ) : null
    }

    static Date parseDateTime( String date ) {
        return date ? iso8601DateTimeFormatter.parse( date ) : null
    }

    static String format( Date date ) {
        return date ? iso8601DateFormatter.format( date ) : null
    }

    /* static String formatDateDBDateTime( Date date ) {
        return date ? iso8601DateTimeFormatter.format( date ) : null
    } */

    static String formatDateDBDateTime( String date ) {
        Instant instant = Instant.parse(date)
        return Timestamp.from(instant)

    }

    static String formatZonedDBDateTime( ZonedDateTime date ) {
        return date ? iso860DBDateTimeFormatter.format( date ) : null
    }

    static String formatLocalFromDB( Date date, String timezone ) {
        ZonedDateTime local = toLocal(date, timezone)
        String formatted = formatZonedDBDateTime(local)
        return formatted
    }

    /* static Date toUTC( String date, String timezone ) {
        LocalDateTime localDateTime = LocalDateTime.parse(date.replaceAll(' ', 'T'))
        ZonedDateTime ptDate = localDateTime.atZone(ZoneId.of(timezone?: User.PT_TIMEZONE))

        return Date.from(ptDate.withZoneSameInstant(ZoneId.of("UTC")).toInstant())
    }

    static ZonedDateTime toLocal( Date date, String timezone ) {
        LocalDateTime utcLocalDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        ZonedDateTime utcZoned = utcLocalDate.atZone(ZoneId.systemDefault());
        return utcZoned.withZoneSameInstant(ZoneId.of(timezone?:User.PT_TIMEZONE))

    }*/

}
