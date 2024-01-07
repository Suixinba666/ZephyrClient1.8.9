package tech.imxianyu.utils.logging;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Formatting;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * currently unused
 * @author ImXianyu
 * @since 3/26/2023 12:23 PM
 */
public class ZLog {

    @Getter
    private static final List<String> allLogs = Collections.synchronizedList(new ArrayList<>());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
    @Getter
    @Setter
    private static LogLevel defaultLogLevel = LogLevel.DEBUG;

    private static void print(String symbol, String msg) {
//        String msg = "[" + getLocalTime() + "] [Zephyr" + "/" + symbol + "] " + message;

        if ("ERROR".equals(symbol))
            msg = Formatting.RED + msg;
        else if ("WARN".equals(symbol))
            msg = Formatting.YELLOW + msg;
        else if ("DEBUG".equals(symbol))
            msg = Formatting.GRAY + msg;
        else // INFO
            msg = Formatting.RESET + msg;

        synchronized (allLogs) {
            allLogs.add(msg);
        }
    }

    public static void d(String msg, Object... args) {
        if (!(defaultLogLevel.level < LogLevel.DEBUG.level))
            return;

        print("DEBUG", String.format(msg, args));
    }

    public static void i(String msg, Object... args) {
        if (!(defaultLogLevel.level < LogLevel.INFO.level))
            return;

        print("INFO", String.format(msg, args));
    }

    public static void w(String msg, Object... args) {
        if (!(defaultLogLevel.level < LogLevel.WARN.level))
            return;

        print("WARN", String.format(msg, args));
    }

    public static void e(String msg, Object... args) {
        if (!(defaultLogLevel.level < LogLevel.ERROR.level))
            return;

        print("ERROR", String.format(msg, args));
    }

    private static String getLocalTime() {
        return dateFormat.format(new Date());
    }


    public enum LogLevel {
        DEBUG(0),
        INFO(1),
        WARN(2),
        ERROR(3),
        NONE(4);

        @Getter
        final int level;

        LogLevel(int level) {
            this.level = level;
        }
    }
}
