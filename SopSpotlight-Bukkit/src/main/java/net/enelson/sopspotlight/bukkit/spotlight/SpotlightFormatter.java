package net.enelson.sopspotlight.bukkit.spotlight;

import net.enelson.sopspotlight.bukkit.SopSpotlightBukkitPlugin;
import net.enelson.sopli.lib.text.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SpotlightFormatter {

    private static final String DEFAULT_START_LINE = "<#675B5B>▏ </#675B5B>";
    private static final int DEFAULT_START_PIXEL_X = 0;
    private static final int DEFAULT_START_PIXEL_Y = 0;
    private static final String DEFAULT_AVATAR_URL = "https://mc-heads.net/avatar/{player}.png/8";

    private final SopSpotlightBukkitPlugin plugin;
    private final MiniMessage miniMessage;
    private final TextUtils textUtils;

    public SpotlightFormatter(SopSpotlightBukkitPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.textUtils = new TextUtils();
    }

    public SpotlightPayload buildOutboundPayload(Player contextPlayer, String playerName, String explicitServerId) {
        String serverId = resolveServerId(explicitServerId);
        String serverDisplayName = serverId;
        ConfigurationSection spotlightSection = plugin.getConfig().getConfigurationSection("spotlight");
        boolean sendAvatar = spotlightSection == null || spotlightSection.getBoolean("send-avatar", true);
        String senderTemplate = spotlightSection == null
                ? "{player}"
                : spotlightSection.getString("sender-text", "{player}");
        String senderText = applyPlaceholderApi(
                contextPlayer,
                replaceBasePlaceholders(senderTemplate, playerName, serverId, serverDisplayName, "")
        );
        List<String> configuredLines = spotlightSection == null
                ? Collections.singletonList("{sender-text}")
                : spotlightSection.getStringList("lines");
        if (configuredLines.isEmpty()) {
            configuredLines = Collections.singletonList("{sender-text}");
        }
        return new SpotlightPayload(playerName, serverId, sendAvatar, senderText);
    }

    public List<Component> buildRecipientLines(Player recipient, SpotlightPayload payload) {
        ConfigurationSection spotlightSection = plugin.getConfig().getConfigurationSection("spotlight");
        List<String> configuredLines = spotlightSection == null
                ? Collections.singletonList("{sender-text}")
                : spotlightSection.getStringList("lines");
        if (configuredLines.isEmpty()) {
            configuredLines = Collections.singletonList("{sender-text}");
        }

        List<String> renderedLines = new ArrayList<String>();
        for (String configuredLine : configuredLines) {
            String line = replaceBasePlaceholders(
                    configuredLine,
                    payload.getPlayerName(),
                    payload.getOriginServerId(),
                    payload.getOriginServerId(),
                    payload.getSenderText()
            );
            renderedLines.add(applyPlaceholderApi(recipient, line));
        }
        return buildRenderedLines(payload.getPlayerName(), payload.isSendAvatar(), renderedLines);
    }

    private String resolveServerId(String explicitServerId) {
        if (explicitServerId != null && !explicitServerId.trim().isEmpty()) {
            return explicitServerId.trim();
        }
        String configured = plugin.getConfig().getString("spotlight.server-id", "");
        if (configured != null && !configured.trim().isEmpty()) {
            return configured.trim();
        }
        try {
            Method method = Bukkit.getServer().getClass().getMethod("getServerName");
            Object value = method.invoke(Bukkit.getServer());
            return value == null ? "" : String.valueOf(value).trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    private String replaceBasePlaceholders(String value, String playerName, String serverId, String serverDisplayName, String senderText) {
        return (value == null ? "" : value)
                .replace("{player}", playerName == null ? "" : playerName)
                .replace("{server}", serverId == null ? "" : serverId)
                .replace("{server-name}", serverDisplayName == null ? "" : serverDisplayName)
                .replace("{sender-text}", senderText == null ? "" : senderText);
    }

    private String applyPlaceholderApi(Player player, String input) {
        if (player == null || input == null || !Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return input == null ? "" : input;
        }
        try {
            Class<?> clazz = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Method method = clazz.getMethod("setPlaceholders", Player.class, String.class);
            Object value = method.invoke(null, player, input);
            return value == null ? input : String.valueOf(value);
        } catch (Throwable ignored) {
            return input;
        }
    }

    private List<Component> buildRenderedLines(String playerName, boolean sendAvatar, List<String> renderedLines) {
        List<Component> lines = new ArrayList<Component>();
        if (renderedLines == null || renderedLines.isEmpty()) {
            return lines;
        }
        lines.add(miniMessage.deserialize(textUtils.prepareMiniMessage(renderedLines.get(0))));

        BufferedImage image = sendAvatar ? loadHeadImage(playerName) : null;
        if (image != null) {
            Component prefix = miniMessage.deserialize(DEFAULT_START_LINE);
            for (int y = DEFAULT_START_PIXEL_Y; y < DEFAULT_START_PIXEL_Y + 8 && y < image.getHeight(); y++) {
                Component line = prefix;
                for (int x = DEFAULT_START_PIXEL_X; x < DEFAULT_START_PIXEL_X + 8 && x < image.getWidth(); x++) {
                    line = line.append(Component.text("█").color(TextColor.color(image.getRGB(x, y))));
                }
                int renderedIndex = (y - DEFAULT_START_PIXEL_Y) + 1;
                if (renderedIndex < renderedLines.size()) {
                    line = line.append(miniMessage.deserialize(textUtils.prepareMiniMessage(renderedLines.get(renderedIndex))));
                }
                lines.add(line);
            }
        } else {
            for (int index = 1; index <= 8; index++) {
                if (index < renderedLines.size()) {
                    lines.add(miniMessage.deserialize(textUtils.prepareMiniMessage(renderedLines.get(index))));
                }
            }
        }

        if (renderedLines.size() > 9) {
            lines.add(miniMessage.deserialize(textUtils.prepareMiniMessage(renderedLines.get(9))));
        }
        return lines;
    }

    private BufferedImage loadHeadImage(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return null;
        }
        String resolvedUrl = DEFAULT_AVATAR_URL.replace("{player}", playerName.trim());
        try {
            return ImageIO.read(new URL(resolvedUrl));
        } catch (IOException ignored) {
            return null;
        }
    }
}
