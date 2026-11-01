package pluginsfix.icenewplayers.text;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorTranslator {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private ColorTranslator() {
    }

    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        String withoutItalic = message.replace("&o", "").replace("§o", "");
        Matcher matcher = HEX_PATTERN.matcher(withoutItalic);
        StringBuilder builder = new StringBuilder(withoutItalic.length() + 32);

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            ChatColor hexColor = ChatColor.of("#" + hexCode);
            matcher.appendReplacement(builder, hexColor.toString());
        }
        matcher.appendTail(builder);

        return ChatColor.translateAlternateColorCodes('&', builder.toString()).replace("§o", "");
    }
}
