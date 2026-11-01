package pluginsfix.icenewplayers.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

class JsonPlayerHistoryRepositoryTest {

    @Test
    void shouldTrackAndPersistPlayers(@TempDir Path tempDir) {
        Logger logger = Logger.getLogger("TestLogger");
        JsonPlayerHistoryRepository repository = new JsonPlayerHistoryRepository(tempDir, logger);
        repository.load();

        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        assertThat(repository.isProcessed(player1)).isFalse();
        assertThat(repository.isProcessed(player2)).isFalse();

        repository.markProcessed(player1);
        assertThat(repository.isProcessed(player1)).isTrue();
        assertThat(repository.isProcessed(player2)).isFalse();

        JsonPlayerHistoryRepository reloaded = new JsonPlayerHistoryRepository(tempDir, logger);
        reloaded.load();
        assertThat(reloaded.isProcessed(player1)).isTrue();
        assertThat(reloaded.isProcessed(player2)).isFalse();

        reloaded.reset(player1);
        assertThat(reloaded.isProcessed(player1)).isFalse();

        JsonPlayerHistoryRepository reloadedAfterReset = new JsonPlayerHistoryRepository(tempDir, logger);
        reloadedAfterReset.load();
        assertThat(reloadedAfterReset.isProcessed(player1)).isFalse();
    }
}
