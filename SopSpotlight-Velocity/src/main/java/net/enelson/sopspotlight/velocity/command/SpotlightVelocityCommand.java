package net.enelson.sopspotlight.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.enelson.sopspotlight.velocity.SopSpotlightVelocityPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class SpotlightVelocityCommand implements SimpleCommand {

    private final SopSpotlightVelocityPlugin plugin;
    private final MiniMessage miniMessage;

    public SpotlightVelocityCommand(SopSpotlightVelocityPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!hasPermission(source)) {
            source.sendMessage(miniMessage.deserialize("<red>You do not have permission to do this.</red>"));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length == 0) {
            sendUsage(source);
            return;
        }

        if ("reload".equalsIgnoreCase(args[0])) {
            plugin.reloadPlugin();
            source.sendMessage(miniMessage.deserialize("<green>SopSpotlight-Velocity reloaded.</green>"));
            return;
        }

        if ("send".equalsIgnoreCase(args[0])) {
            String playerName = args.length >= 2 ? args[1] : "";
            String serverName = args.length >= 3 ? args[2] : "";
            if (playerName.isEmpty() && source instanceof Player) {
                Player player = (Player) source;
                playerName = player.getUsername();
                if (serverName.isEmpty()) {
                    serverName = plugin.getSpotlightService().resolvePlayerServer(player);
                }
            }
            if (playerName.isEmpty()) {
                sendUsage(source);
                return;
            }
            plugin.getSpotlightService().broadcastSpotlight(playerName, serverName);
            source.sendMessage(miniMessage.deserialize("<green>Spotlight broadcast sent.</green>"));
            return;
        }

        sendUsage(source);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (!hasPermission(invocation.source())) {
            return Collections.emptyList();
        }
        String[] args = invocation.arguments();
        if (args.length <= 1) {
            return filter(Arrays.asList("send", "reload"), args.length == 0 ? "" : args[0]);
        }
        if (args.length == 2 && "send".equalsIgnoreCase(args[0])) {
            List<String> names = new ArrayList<String>();
            for (Player player : plugin.getServer().getAllPlayers()) {
                names.add(player.getUsername());
            }
            return filter(names, args[1]);
        }
        return Collections.emptyList();
    }

    private void sendUsage(CommandSource source) {
        source.sendMessage(miniMessage.deserialize("<yellow>/spotlight send <player> [server]</yellow>"));
    }

    private boolean hasPermission(CommandSource source) {
        return source.hasPermission("sopspotlight.admin");
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
}
