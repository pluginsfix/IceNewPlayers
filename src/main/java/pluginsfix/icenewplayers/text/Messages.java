package pluginsfix.icenewplayers.text;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class Messages {
    private final Plugin plugin;
    private final Map<String, List<String>> messageCache = new ConcurrentHashMap<>();

    public Messages(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public synchronized void reload() {
        messageCache.clear();
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
            );
            config.setDefaults(defaultConfig);
        }

        for (String key : config.getKeys(true)) {
            if (config.isList(key)) {
                List<String> list = config.getStringList(key);
                messageCache.put(key, List.copyOf(list));
            } else if (config.isString(key)) {
                String single = config.getString(key);
                if (single != null) {
                    messageCache.put(key, List.of(single));
                }
            }
        }
    }

    public List<String> get(String key) {
        return messageCache.getOrDefault(key, Collections.emptyList());
    }

    public void send(CommandSender sender, String key, String... placeholders) {
        if (sender == null) {
            return;
        }

        List<String> lines = messageCache.get(key);
        if (lines == null || lines.isEmpty()) {
            plugin.getLogger().log(Level.WARNING, "Missing message key in messages.yml: " + key);
            return;
        }

        Player player = (sender instanceof Player p) ? p : null;

        for (String rawLine : lines) {
            String processed = applyPlaceholders(rawLine, placeholders);
            dispatchLine(sender, player, processed);
        }
    }

    private void dispatchLine(CommandSender sender, Player player, String line) {
        if (line.startsWith("[message] ")) {
            String text = line.substring(10);
            sender.sendMessage(ColorTranslator.colorize(text));
        } else if (line.startsWith("[actionbar] ")) {
            if (player != null) {
                String text = line.substring(12);
                player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(ColorTranslator.colorize(text))
                );
            }
        } else if (line.startsWith("[title] ")) {
            if (player != null) {
                handleTitle(player, line.substring(8));
            }
        } else if (line.startsWith("[sound] ")) {
            if (player != null) {
                handleSound(player, line.substring(8).trim());
            }
        } else {
            sender.sendMessage(ColorTranslator.colorize(line));
        }
    }

    private void handleTitle(Player player, String rawData) {
        String[] parts = rawData.split(";");
        String title = parts.length > 0 ? ColorTranslator.colorize(parts[0]) : "";
        String subtitle = parts.length > 1 ? ColorTranslator.colorize(parts[1]) : "";
        int fadeIn = parts.length > 2 ? parseInteger(parts[2], 10) : 10;
        int stay = parts.length > 3 ? parseInteger(parts[3], 40) : 40;
        int fadeOut = parts.length > 4 ? parseInteger(parts[4], 10) : 10;

        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    private void handleSound(Player player, String soundName) {
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private String applyPlaceholders(String source, String... placeholders) {
        if (placeholders.length < 2) {
            return source;
        }

        String result = source;
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            String target = placeholders[i];
            String replacement = placeholders[i + 1];
            if (target != null && replacement != null) {
                result = result.replace(target, replacement);
            }
        }
        return result;
    }

    private int parseInteger(String raw, int fallback) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
