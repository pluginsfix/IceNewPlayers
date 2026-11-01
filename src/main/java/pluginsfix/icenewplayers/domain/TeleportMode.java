package pluginsfix.icenewplayers.domain;

import java.util.Locale;

public enum TeleportMode {
    COMMAND,
    LOCATION,
    BOTH,
    CONSOLE;

    public static TeleportMode fromString(String value) {
        if (value == null) {
            return COMMAND;
        }
        try {
            return TeleportMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return COMMAND;
        }
    }
}
