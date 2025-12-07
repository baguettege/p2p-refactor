package util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {
    /// simply utility for time

    // returns time in the format HH:mm:ss, e.g. 17:58:34
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static String timeNow() {
        return LocalTime.now().format(timeFormatter);
    }
}
