package pluginsfix.icenewplayers.domain;

import java.util.UUID;

public interface PlayerHistoryRepository {
    boolean isProcessed(UUID uniqueId);

    void markProcessed(UUID uniqueId);

    void reset(UUID uniqueId);

    void load();

    void save();

    void close();
}
