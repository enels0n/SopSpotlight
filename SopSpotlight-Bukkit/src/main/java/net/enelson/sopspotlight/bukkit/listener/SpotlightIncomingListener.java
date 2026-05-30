package net.enelson.sopspotlight.bukkit.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.enelson.sopspotlight.bukkit.SopSpotlightBukkitPlugin;
import net.enelson.sopspotlight.bukkit.spotlight.SpotlightPayload;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.lang.reflect.Method;
import java.util.List;

public final class SpotlightIncomingListener implements PluginMessageListener {

    private final SopSpotlightBukkitPlugin plugin;

    public SpotlightIncomingListener(SopSpotlightBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!SopSpotlightBukkitPlugin.CHANNEL.equals(channel)) {
            return;
        }

        ByteArrayDataInput input = ByteStreams.newDataInput(message);
        String action = input.readUTF();
        if (!"deliver".equalsIgnoreCase(action)) {
            return;
        }

        SpotlightPayload payload = new SpotlightPayload(
                input.readUTF(),
                input.readUTF(),
                input.readBoolean(),
                input.readUTF()
        );

        String localServerId = resolveLocalServerId();
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            if (!localServerId.isEmpty()
                    && localServerId.equalsIgnoreCase(payload.getOriginServerId())
                    && !recipient.getName().equalsIgnoreCase(payload.getPlayerName())) {
                continue;
            }
            List<Component> lines = plugin.getSpotlightFormatter().buildRecipientLines(recipient, payload);
            for (Component line : lines) {
                BaseComponent[] components = ComponentSerializer.parse(GsonComponentSerializer.gson().serialize(line));
                recipient.spigot().sendMessage(components);
            }
        }
    }

    private String resolveLocalServerId() {
        String configured = plugin.getConfig().getString("spotlight.server-id", "").trim();
        if (!configured.isEmpty()) {
            return configured;
        }
        try {
            Method method = Bukkit.getServer().getClass().getMethod("getServerName");
            Object value = method.invoke(Bukkit.getServer());
            return value == null ? "" : String.valueOf(value).trim();
        } catch (Exception ignored) {
            return "";
        }
    }
}
