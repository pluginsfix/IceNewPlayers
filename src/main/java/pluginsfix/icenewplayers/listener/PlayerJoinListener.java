package pluginsfix.icenewplayers.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pluginsfix.icenewplayers.config.PluginConfig;
import pluginsfix.icenewplayers.service.FirstSpawnService;

public final class PlayerJoinListener implements Listener {
    private final FirstSpawnService service;
    private PluginConfig config;

    public PlayerJoinListener(FirstSpawnService service, PluginConfig config) {
        this.service = service;
        this.config = config;
    }

    public void updateConfig(PluginConfig config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!config.onFirstJoin()) {
            return;
        }

        Player player = event.getPlayer();
        if (service.isNewPlayer(player)) {
            service.scheduleFirstSpawn(player, "First-Join");
        }
    }
}
