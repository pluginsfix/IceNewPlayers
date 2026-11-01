package pluginsfix.icenewplayers.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import pluginsfix.icenewplayers.domain.PlayerHistoryRepository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class JsonPlayerHistoryRepository implements PlayerHistoryRepository {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type SET_TYPE = new TypeToken<Set<String>>() {}.getType();

    private final Path filePath;
    private final Logger logger;
    private final Set<UUID> processedPlayers = ConcurrentHashMap.newKeySet();
    private volatile boolean dirty = false;

    public JsonPlayerHistoryRepository(Path dataFolder, Logger logger) {
        this.filePath = dataFolder.resolve("history.json");
        this.logger = logger;
    }

    @Override
    public boolean isProcessed(UUID uniqueId) {
        if (uniqueId == null) {
            return false;
        }
        return processedPlayers.contains(uniqueId);
    }

    @Override
    public void markProcessed(UUID uniqueId) {
        if (uniqueId == null) {
            return;
        }
        if (processedPlayers.add(uniqueId)) {
            dirty = true;
            save();
        }
    }

    @Override
    public void reset(UUID uniqueId) {
        if (uniqueId == null) {
            return;
        }
        if (processedPlayers.remove(uniqueId)) {
            dirty = true;
            save();
        }
    }

    @Override
    public synchronized void load() {
        processedPlayers.clear();
        dirty = false;

        if (!Files.exists(filePath)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            Set<String> stringSet = GSON.fromJson(reader, SET_TYPE);
            if (stringSet != null) {
                for (String raw : stringSet) {
                    try {
                        processedPlayers.add(UUID.fromString(raw));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not load history.json", e);
        }
    }

    @Override
    public synchronized void save() {
        if (!dirty) {
            return;
        }

        try {
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            Path tempFile = filePath.resolveSibling("history.json.tmp");
            Set<String> stringSet = ConcurrentHashMap.newKeySet();
            for (UUID uuid : processedPlayers) {
                stringSet.add(uuid.toString());
            }

            try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
                GSON.toJson(stringSet, SET_TYPE, writer);
            }

            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            dirty = false;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save history.json", e);
        }
    }

    @Override
    public void close() {
        save();
    }

    public Set<UUID> getProcessedPlayers() {
        return Collections.unmodifiableSet(processedPlayers);
    }
}
