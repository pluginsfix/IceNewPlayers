package pluginsfix.icenewplayers.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeleportModeTest {

    @Test
    void shouldParseKnownModes() {
        assertThat(TeleportMode.fromString("COMMAND")).isEqualTo(TeleportMode.COMMAND);
        assertThat(TeleportMode.fromString("command")).isEqualTo(TeleportMode.COMMAND);
        assertThat(TeleportMode.fromString("LOCATION")).isEqualTo(TeleportMode.LOCATION);
        assertThat(TeleportMode.fromString("BOTH")).isEqualTo(TeleportMode.BOTH);
        assertThat(TeleportMode.fromString("CONSOLE")).isEqualTo(TeleportMode.CONSOLE);
    }

    @Test
    void shouldFallbackToCommandOnInvalid() {
        assertThat(TeleportMode.fromString(null)).isEqualTo(TeleportMode.COMMAND);
        assertThat(TeleportMode.fromString("UNKNOWN_MODE")).isEqualTo(TeleportMode.COMMAND);
        assertThat(TeleportMode.fromString("")).isEqualTo(TeleportMode.COMMAND);
    }
}
