package pluginsfix.icenewplayers.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import pluginsfix.icenewplayers.config.PluginConfig;
import pluginsfix.icenewplayers.config.SpawnLocation;
import pluginsfix.icenewplayers.service.FirstSpawnService;
import pluginsfix.icenewplayers.text.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class IceNewPlayersCommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin;
    private final FirstSpawnService service;
    private final Messages messages;
    private final Consumer<PluginConfig> onConfigReload;

    public IceNewPlayersCommand(
            Plugin plugin,
            FirstSpawnService service,
            Messages messages,
            Consumer<PluginConfig> onConfigReload
    ) {
        this.plugin = plugin;
        this.service = service;
        this.messages = messages;
        this.onConfigReload = onConfigReload;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!sender.hasPermission("icenewplayers.use")) {
            messages.send(sender, "no-permission");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            messages.send(sender, "help");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> handleReload(sender);
            case "setspawn" -> handleSetSpawn(sender);
            case "spawn" -> handleSpawn(sender);
            case "reset" -> handleReset(sender, args);
            case "status" -> handleStatus(sender, args);
            default -> messages.send(sender, "help");
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("icenewplayers.admin.reload") && !sender.hasPermission("icenewplayers.admin")) {
            messages.send(sender, "no-permission");
            return;
        }

        messages.reload();
        PluginConfig newConfig = PluginConfig.load(plugin);
        service.updateConfig(newConfig);
        onConfigReload.accept(newConfig);

        messages.send(sender, "reloaded");
    }

    private void handleSetSpawn(CommandSender sender) {
        if (!sender.hasPermission("icenewplayers.admin.setspawn") && !sender.hasPermission("icenewplayers.admin")) {
            messages.send(sender, "no-permission");
            return;
        }

        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return;
        }

        Location loc = player.getLocation();
        SpawnLocation spawnLoc = new SpawnLocation(
                loc.getWorld() != null ? loc.getWorld().getName() : "world",
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()
        );

        PluginConfig.saveSpawnLocation(plugin, spawnLoc);
        PluginConfig updatedConfig = PluginConfig.load(plugin);
        service.updateConfig(updatedConfig);
        onConfigReload.accept(updatedConfig);

        messages.send(player, "spawn-set");
    }

    private void handleSpawn(CommandSender sender) {
        if (!sender.hasPermission("icenewplayers.admin.spawn") && !sender.hasPermission("icenewplayers.admin")) {
            messages.send(sender, "no-permission");
            return;
        }

        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return;
        }

        service.executeTeleport(player);
        messages.send(player, "spawn-teleported");
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("icenewplayers.admin.reset") && !sender.hasPermission("icenewplayers.admin")) {
            messages.send(sender, "no-permission");
            return;
        }

        if (args.length < 2) {
            messages.send(sender, "help");
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            messages.send(sender, "player-not-found");
            return;
        }

        service.resetPlayer(target.getUniqueId());
        messages.send(sender, "player-reset", "{player}", target.getName());
    }

    private void handleStatus(CommandSender sender, String[] args) {
        if (!sender.hasPermission("icenewplayers.admin.status") && !sender.hasPermission("icenewplayers.admin")) {
            messages.send(sender, "no-permission");
            return;
        }

        if (args.length < 2) {
            messages.send(sender, "help");
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            messages.send(sender, "player-not-found");
            return;
        }

        if (service.isNewPlayer(target)) {
            messages.send(sender, "player-status-new", "{player}", target.getName());
        } else {
            messages.send(sender, "player-status-processed", "{player}", target.getName());
        }
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!sender.hasPermission("icenewplayers.use")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            if (sender.hasPermission("icenewplayers.admin.reload") || sender.hasPermission("icenewplayers.admin")) {
                subCommands.add("reload");
            }
            if (sender.hasPermission("icenewplayers.admin.setspawn") || sender.hasPermission("icenewplayers.admin")) {
                subCommands.add("setspawn");
            }
            if (sender.hasPermission("icenewplayers.admin.spawn") || sender.hasPermission("icenewplayers.admin")) {
                subCommands.add("spawn");
            }
            if (sender.hasPermission("icenewplayers.admin.reset") || sender.hasPermission("icenewplayers.admin")) {
                subCommands.add("reset");
            }
            if (sender.hasPermission("icenewplayers.admin.status") || sender.hasPermission("icenewplayers.admin")) {
                subCommands.add("status");
            }
            subCommands.add("help");

            String query = args[0].toLowerCase();
            return subCommands.stream()
                    .filter(s -> s.startsWith(query))
                    .toList();
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("status"))) {
            String query = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(query))
                    .toList();
        }

        return Collections.emptyList();
    }
}
