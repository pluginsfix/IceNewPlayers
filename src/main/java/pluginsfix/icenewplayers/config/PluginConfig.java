package pluginsfix.icenewplayers.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import pluginsfix.icenewplayers.domain.TeleportMode;

public record PluginConfig(
        long delayTicks,
        boolean onFirstJoin,
        boolean onAuthMeRegister,
        boolean onAuthMeLogin,
        TeleportMode mode,
        String playerCommand,
        String consoleCommand,
        boolean customLocationEnabled,
        SpawnLocation spawnLocation,
        boolean notificationEnabled,
        String sound,
        float soundVolume,
        float soundPitch
) {
    public static PluginConfig load(Plugin plugin) {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        long delayTicks = Math.max(0L, config.getLong("teleport.delay-ticks", 5L));
        boolean onFirstJoin = config.getBoolean("teleport.on-first-join", true);
        boolean onAuthMeRegister = config.getBoolean("teleport.on-authme-register", true);
        boolean onAuthMeLogin = config.getBoolean("teleport.on-authme-login", false);
        TeleportMode mode = TeleportMode.fromString(config.getString("teleport.mode", "COMMAND"));
        String playerCommand = config.getString("teleport.player-command", "spawn");
        String consoleCommand = config.getString("teleport.console-command", "spawn {player}");

        boolean customLocationEnabled = config.getBoolean("teleport.spawn-location.enabled", false);
        String world = config.getString("teleport.spawn-location.world", "world");
        double x = config.getDouble("teleport.spawn-location.x", 0.5);
        double y = config.getDouble("teleport.spawn-location.y", 64.0);
        double z = config.getDouble("teleport.spawn-location.z", 0.5);
        float yaw = (float) config.getDouble("teleport.spawn-location.yaw", 0.0);
        float pitch = (float) config.getDouble("teleport.spawn-location.pitch", 0.0);
        SpawnLocation spawnLocation = new SpawnLocation(world, x, y, z, yaw, pitch);

        boolean notificationEnabled = config.getBoolean("notification.enabled", true);
        String sound = config.getString("notification.sound", "ENTITY_PLAYER_LEVELUP");
        float soundVolume = (float) config.getDouble("notification.sound-volume", 1.0);
        float soundPitch = (float) config.getDouble("notification.sound-pitch", 1.0);

        return new PluginConfig(
                delayTicks,
                onFirstJoin,
                onAuthMeRegister,
                onAuthMeLogin,
                mode,
                playerCommand,
                consoleCommand,
                customLocationEnabled,
                spawnLocation,
                notificationEnabled,
                sound,
                soundVolume,
                soundPitch
        );
    }

    public static void saveSpawnLocation(Plugin plugin, SpawnLocation location) {
        FileConfiguration config = plugin.getConfig();
        config.set("teleport.spawn-location.enabled", true);
        config.set("teleport.spawn-location.world", location.world());
        config.set("teleport.spawn-location.x", location.x());
        config.set("teleport.spawn-location.y", location.y());
        config.set("teleport.spawn-location.z", location.z());
        config.set("teleport.spawn-location.yaw", (double) location.yaw());
        config.set("teleport.spawn-location.pitch", (double) location.pitch());
        plugin.saveConfig();
    }
}
