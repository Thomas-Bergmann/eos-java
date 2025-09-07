package de.hatoka.eos.devices.internal.business;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateTooling
{
    private static final ZoneId berlinTz = ZoneId.of("Europe/Berlin");

    public static final ZonedDateTime SOMMER_SUN = createBerlinDate("2024/06/21").withHour(12);
    public static final ZonedDateTime SOMMER_NIGHT = createBerlinDate("2024/06/21");

    public static ZonedDateTime createBerlinDate(String date)
    {
        String[] dateParts = date.split("/");
        var year = Integer.parseInt(dateParts[0]);
        var month = Integer.parseInt(dateParts[1]);
        var dayOfMonth = Integer.parseInt(dateParts[2]);
        return LocalDateTime.of(year, month, dayOfMonth, 0, 0).withSecond(0).withNano(0).atZone(berlinTz);
    }
}
