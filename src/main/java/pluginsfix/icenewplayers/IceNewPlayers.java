package pluginsfix.icenewplayers;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import pluginsfix.icenewplayers.command.IceNewPlayersCommand;
import pluginsfix.icenewplayers.config.PluginConfig;
import pluginsfix.icenewplayers.domain.PlayerHistoryRepository;
import pluginsfix.icenewplayers.hook.AuthMeHook;
import pluginsfix.icenewplayers.listener.PlayerJoinListener;
import pluginsfix.icenewplayers.service.FirstSpawnService;
import pluginsfix.icenewplayers.storage.JsonPlayerHistoryRepository;
import pluginsfix.icenewplayers.text.Messages;

public final class IceNewPlayers extends JavaPlugin {
    private PlayerHistoryRepository repository;
    private Messages messages;
    private FirstSpawnService service;
    private PlayerJoinListener joinListener;
    private AuthMeHook authMeHook;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        PluginConfig config = PluginConfig.load(this);

        messages = new Messages(this);
        repository = new JsonPlayerHistoryRepository(getDataFolder().toPath(), getLogger());
        repository.load();

        service = new FirstSpawnService(this, repository, messages, config);

        joinListener = new PlayerJoinListener(service, config);
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(joinListener, this);

        if (pm.isPluginEnabled("AuthMe")) {
            authMeHook = new AuthMeHook(this, service, config);
            getLogger().info("Hooked into AuthMe successfully.");
        }

        PluginCommand command = getCommand("icenewplayers");
        if (command != null) {
            IceNewPlayersCommand executor = new IceNewPlayersCommand(
                    this,
                    service,
                    messages,
                    this::onConfigReloaded
            );
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    private void onConfigReloaded(PluginConfig newConfig) {
        if (joinListener != null) {
            joinListener.updateConfig(newConfig);
        }
        if (authMeHook != null) {
            authMeHook.updateConfig(newConfig);
        }
    }

    @Override
    public void onDisable() {
        if (repository != null) {
            repository.close();
        }
    }
}
