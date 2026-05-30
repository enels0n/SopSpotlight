package net.enelson.sopspotlight.bukkit.command;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.enelson.sopspotlight.bukkit.SopSpotlightBukkitPlugin;
import net.enelson.sopspotlight.bukkit.spotlight.SpotlightPayload;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class SpotlightCommand implements CommandExecutor, TabCompleter {

    private final SopSpotlightBukkitPlugin plugin;

    public SpotlightCommand(SopSpotlightBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(color(plugin.getConfig().getString("messages.usage", "&e/spotlight send [player] [server]")));
            return true;
        }
        if ("reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sopspotlight.reload")) {
                sender.sendMessage(color(plugin.getConfig().getString("messages.no-permission", "&cNo permission.")));
                return true;
            }
            plugin.reloadPlugin();
            sender.sendMessage(color(plugin.getConfig().getString("messages.reloaded", "&aSopSpotlight-Bukkit reloaded.")));
            return true;
        }
        if (!"send".equalsIgnoreCase(args[0])) {
            sender.sendMessage(color(plugin.getConfig().getString("messages.usage", "&e/spotlight send [player] [server]")));
            return true;
        }
        if (!sender.hasPermission("sopspotlight.send")) {
            sender.sendMessage(color(plugin.getConfig().getString("messages.no-permission", "&cNo permission.")));
            return true;
        }

        String playerName = args.length >= 2 ? args[1] : "";
        String serverName = args.length >= 3 ? args[2] : "";
        Player carrier;

        if (sender instanceof Player) {
            carrier = (Player) sender;
            long remainingCooldown = plugin.getRemainingCooldownSeconds(carrier);
            if (remainingCooldown > 0L) {
                sender.sendMessage(color(plugin.getConfig().getString("messages.cooldown", "&cYou can use spotlight again in {seconds}s.")
                        .replace("{seconds}", String.valueOf(remainingCooldown))));
                return true;
            }
            if (playerName.isEmpty()) {
                playerName = carrier.getName();
            }
        } else {
            if (playerName.isEmpty()) {
                sender.sendMessage(color(plugin.getConfig().getString("messages.console-player-required", "&cConsole must specify a player.")));
                return true;
            }
            carrier = Bukkit.getPlayerExact(playerName);
            if (carrier == null) {
                sender.sendMessage(color(plugin.getConfig().getString("messages.player-offline", "&cPlayer is offline.")));
                return true;
            }
        }

        SpotlightPayload payload = plugin.getSpotlightFormatter().buildOutboundPayload(carrier, playerName, serverName);
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("broadcast");
        output.writeUTF(payload.getPlayerName());
        output.writeUTF(payload.getOriginServerId());
        output.writeBoolean(payload.isSendAvatar());
        output.writeUTF(payload.getSenderText());
        carrier.sendPluginMessage(plugin, SopSpotlightBukkitPlugin.CHANNEL, output.toByteArray());
        if (sender instanceof Player) {
            plugin.applyCooldown((Player) sender);
        }
        sender.sendMessage(color(plugin.getConfig().getString("messages.sent", "&aSpotlight request sent.")));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<String>();
            if (sender.hasPermission("sopspotlight.send")) {
                options.add("send");
            }
            if (sender.hasPermission("sopspotlight.reload")) {
                options.add("reload");
            }
            return filter(options, args[0]);
        }
        if (args.length == 2 && "send".equalsIgnoreCase(args[0]) && sender.hasPermission("sopspotlight.send")) {
            List<String> players = new ArrayList<String>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return filter(players, args[1]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> values, String input) {
        List<String> result = new ArrayList<String>();
        String lower = input.toLowerCase(Locale.ROOT);
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(value);
            }
        }
        return result;
    }

    private String color(String text) {
        return text == null ? "" : text.replace("&", "\u00A7");
    }
}
