package pluginsfix.icenewplayers.hook;

import fr.xephi.authme.events.LoginEvent;
import fr.xephi.authme.events.RegisterEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import pluginsfix.icenewplayers.config.PluginConfig;
import pluginsfix.icenewplayers.service.FirstSpawnService;

public final class AuthMeHook implements Listener {
    private final Plugin plugin;
    private final FirstSpawnService service;
    private PluginConfig config;

    public AuthMeHook(Plugin plugin, FirstSpawnService service, PluginConfig config) {
        this.plugin = plugin;
        this.service = service;
        this.config = config;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void updateConfig(PluginConfig config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRegister(RegisterEvent event) {
        if (!config.onAuthMeRegister()) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null || !player.isOnline()) {
            return;
        }

        service.scheduleFirstSpawn(player, "AuthMe-Register");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLogin(LoginEvent event) {
        if (!config.onAuthMeLogin()) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null || !player.isOnline()) {
            return;
        }

        if (service.isNewPlayer(player)) {
            service.scheduleFirstSpawn(player, "AuthMe-Login");
        }
    }
}
