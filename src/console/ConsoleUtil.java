package console;

import util.TimeUtil;

public class ConsoleUtil {
    /// simply util for consoles

    private static final String INDENT = " ".repeat(11);
    protected static String formatLog(String text) {
        if (text.isBlank()) return TimeUtil.timeNow() + " | " + text;

        String[] parts = text.split("\n");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= parts.length - 1; i++) {
            if (i == 0) {
                sb.append(TimeUtil.timeNow()).append(" | ").append(parts[i]);
            } else {
                sb.append("\n").append(INDENT).append(parts[i]);
            }
        }

        return sb.toString();
    }
}
