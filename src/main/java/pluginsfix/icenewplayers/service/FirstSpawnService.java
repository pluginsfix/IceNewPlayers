package pluginsfix.icenewplayers.service;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import pluginsfix.icenewplayers.config.PluginConfig;
import pluginsfix.icenewplayers.config.SpawnLocation;
import pluginsfix.icenewplayers.domain.PlayerHistoryRepository;
import pluginsfix.icenewplayers.domain.TeleportMode;
import pluginsfix.icenewplayers.text.Messages;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class FirstSpawnService {
    private final Plugin plugin;
    private final PlayerHistoryRepository repository;
    private final Messages messages;
    private final NamespacedKey pdcKey;
    private final Set<UUID> scheduledTeleports = ConcurrentHashMap.newKeySet();
    private PluginConfig config;

    public FirstSpawnService(Plugin plugin, PlayerHistoryRepository repository, Messages messages, PluginConfig config) {
        this.plugin = plugin;
        this.repository = repository;
        this.messages = messages;
        this.config = config;
        this.pdcKey = new NamespacedKey(plugin, "first_spawn_processed");
    }

    public void updateConfig(PluginConfig newConfig) {
        this.config = newConfig;
    }

    public boolean isNewPlayer(Player player) {
        if (player == null) {
            return false;
        }

        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (pdc.has(pdcKey, PersistentDataType.BYTE)) {
            return false;
        }

        if (repository.isProcessed(player.getUniqueId())) {
            pdc.set(pdcKey, PersistentDataType.BYTE, (byte) 1);
            return false;
        }

        return true;
    }

    public boolean scheduleFirstSpawn(Player player, String triggerReason) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        UUID uniqueId = player.getUniqueId();
        if (!scheduledTeleports.add(uniqueId)) {
            return false;
        }

        markProcessed(player);

        long delayTicks = config.delayTicks();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                scheduledTeleports.remove(uniqueId);
                if (!player.isOnline()) {
                    return;
                }
                executeTeleport(player);
                plugin.getLogger().log(Level.INFO, "Teleported new player " + player.getName() + " to spawn (trigger: " + triggerReason + ")");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to execute first spawn teleport for player " + player.getName(), e);
            }
        }, delayTicks);

        return true;
    }

    public void executeTeleport(Player player) {
        TeleportMode mode = config.mode();

        if (config.customLocationEnabled() || mode == TeleportMode.LOCATION || mode == TeleportMode.BOTH) {
            SpawnLocation target = config.spawnLocation();
            World world = Bukkit.getWorld(target.world());
            if (world != null) {
                Location location = new Location(world, target.x(), target.y(), target.z(), target.yaw(), target.pitch());
                player.teleport(location);
            }
        }

        if (mode == TeleportMode.COMMAND || mode == TeleportMode.BOTH) {
            String playerCmd = config.playerCommand();
            if (playerCmd != null && !playerCmd.isBlank()) {
                String cleanCmd = playerCmd.startsWith("/") ? playerCmd.substring(1) : playerCmd;
                player.performCommand(cleanCmd);
            }
        }

        if (mode == TeleportMode.CONSOLE) {
            String consoleCmd = config.consoleCommand();
            if (consoleCmd != null && !consoleCmd.isBlank()) {
                String formatted = consoleCmd.replace("{player}", player.getName());
                String cleanCmd = formatted.startsWith("/") ? formatted.substring(1) : formatted;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cleanCmd);
            }
        }

        if (config.notificationEnabled()) {
            playNotificationSound(player);
        }

        messages.send(player, "welcome", "{player}", player.getName());
    }

    public void markProcessed(Player player) {
        if (player == null) {
            return;
        }
        player.getPersistentDataContainer().set(pdcKey, PersistentDataType.BYTE, (byte) 1);
        repository.markProcessed(player.getUniqueId());
    }

    public void resetPlayer(UUID uniqueId) {
        if (uniqueId == null) {
            return;
        }
        repository.reset(uniqueId);
        scheduledTeleports.remove(uniqueId);

        Player online = Bukkit.getPlayer(uniqueId);
        if (online != null && online.isOnline()) {
            online.getPersistentDataContainer().remove(pdcKey);
        }
    }

    private void playNotificationSound(Player player) {
        String soundName = config.sound();
        if (soundName == null || soundName.isBlank()) {
            return;
        }
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, config.soundVolume(), config.soundPitch());
        } catch (IllegalArgumentException ignored) {
        }
    }
}
