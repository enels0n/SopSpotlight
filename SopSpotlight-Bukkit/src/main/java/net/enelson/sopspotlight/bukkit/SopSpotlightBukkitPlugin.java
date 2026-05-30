package net.enelson.sopspotlight.bukkit;

import net.enelson.sopspotlight.bukkit.command.SpotlightCommand;
import net.enelson.sopspotlight.bukkit.listener.SpotlightIncomingListener;
import net.enelson.sopspotlight.bukkit.spotlight.SpotlightFormatter;
import org.bukkit.entity.Player;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SopSpotlightBukkitPlugin extends JavaPlugin {

    public static final String CHANNEL = "sopspotlight:main";
    private static final String COOLDOWN_PERMISSION_PREFIX = "sopspotlight.cooldown.";
    private SpotlightFormatter spotlightFormatter;
    private final Map<UUID, Long> nextAvailableSpotlightUse = new ConcurrentHashMap<UUID, Long>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.spotlightFormatter = new SpotlightFormatter(this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
        getServer().getMessenger().registerIncomingPluginChannel(this, CHANNEL, new SpotlightIncomingListener(this));

        PluginCommand command = getCommand("spotlight");
        if (command != null) {
            SpotlightCommand executor = new SpotlightCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, CHANNEL);
        getServer().getMessenger().unregisterIncomingPluginChannel(this, CHANNEL);
    }

    public boolean reloadPlugin() {
        reloadConfig();
        return true;
    }

    public SpotlightFormatter getSpotlightFormatter() {
        return spotlightFormatter;
    }

    public long getRemainingCooldownSeconds(Player player) {
        if (player == null) {
            return 0L;
        }
        Long nextUseAt = nextAvailableSpotlightUse.get(player.getUniqueId());
        if (nextUseAt == null) {
            return 0L;
        }
        long now = System.currentTimeMillis();
        if (nextUseAt.longValue() <= now) {
            nextAvailableSpotlightUse.remove(player.getUniqueId());
            return 0L;
        }
        return Math.max(1L, (nextUseAt.longValue() - now + 999L) / 1000L);
    }

    public void applyCooldown(Player player) {
        if (player == null) {
            return;
        }
        int cooldownSeconds = resolveCooldownSeconds(player);
        if (cooldownSeconds <= 0) {
            nextAvailableSpotlightUse.remove(player.getUniqueId());
            return;
        }
        nextAvailableSpotlightUse.put(player.getUniqueId(), System.currentTimeMillis() + (cooldownSeconds * 1000L));
    }

    public int resolveCooldownSeconds(Player player) {
        int defaultCooldown = Math.max(0, getConfig().getInt("spotlight.default-cooldown-seconds", 300));
        if (player == null) {
            return defaultCooldown;
        }
        int bestCooldown = defaultCooldown;
        for (org.bukkit.permissions.PermissionAttachmentInfo permissionInfo : player.getEffectivePermissions()) {
            if (!permissionInfo.getValue()) {
                continue;
            }
            String permission = permissionInfo.getPermission();
            if (permission == null) {
                continue;
            }
            String lowerPermission = permission.toLowerCase(Locale.ROOT);
            if (!lowerPermission.startsWith(COOLDOWN_PERMISSION_PREFIX)) {
                continue;
            }
            String value = permission.substring(COOLDOWN_PERMISSION_PREFIX.length());
            try {
                int parsed = Integer.parseInt(value);
                if (parsed >= 0 && parsed < bestCooldown) {
                    bestCooldown = parsed;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return bestCooldown;
    }
}
