package io.alcatraz.audiohq;

public class LogBuff {
    private static String log_raw = "";
    private static int count;

    static {
        count = 0;
    }

    public static void log(String log) {
        int LOG_COUNT_MAX = 512;
        if (count >= LOG_COUNT_MAX) {
            log_raw = "";
            count = 0;
        }
        log_raw += log + "\n";
        count++;
    }

    public static String getLog() {
        return log_raw;
    }

    public static int getCount() {
        return count;
    }

}
