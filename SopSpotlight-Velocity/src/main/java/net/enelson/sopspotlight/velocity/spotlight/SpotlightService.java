package net.enelson.sopspotlight.velocity.spotlight;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.enelson.sopspotlight.velocity.SopSpotlightVelocityPlugin;

import java.util.Optional;

public final class SpotlightService {

    private final SopSpotlightVelocityPlugin plugin;

    public SpotlightService(SopSpotlightVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    public void broadcastSpotlight(String playerName, String serverName) {
        forwardSpotlight(playerName, serverName, true, playerName);
    }

    public void forwardSpotlight(String playerName, String originServerId, boolean sendAvatar, String senderText) {
        String effectivePlayer = playerName == null ? "" : playerName.trim();
        String effectiveServer = originServerId == null ? "" : originServerId.trim();
        String effectiveSenderText = senderText == null ? "" : senderText;
        if (effectivePlayer.isEmpty() || effectiveServer.isEmpty()) {
            plugin.getLogger().warn("Spotlight skipped: player='{}', server='{}'", effectivePlayer, effectiveServer);
            return;
        }

        byte[] payload = createPayload(effectivePlayer, effectiveServer, sendAvatar, effectiveSenderText);
        int servers = 0;
        for (RegisteredServer server : plugin.getServer().getAllServers()) {
            if (server.sendPluginMessage(plugin.getChannel(), payload)) {
                servers++;
            }
        }
        debug("Forwarded spotlight: player={}, server={}, targets={}",
                effectivePlayer,
                effectiveServer,
                Integer.valueOf(servers));
    }

    public String resolvePlayerServer(Player player) {
        if (player == null) {
            return "";
        }
        Optional<RegisteredServer> currentServer = player.getCurrentServer().map(connection -> connection.getServer());
        return currentServer.map(server -> server.getServerInfo().getName()).orElse("");
    }

    private byte[] createPayload(String playerName, String originServerId, boolean sendAvatar, String senderText) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("deliver");
        output.writeUTF(playerName);
        output.writeUTF(originServerId);
        output.writeBoolean(sendAvatar);
        output.writeUTF(senderText);
        return output.toByteArray();
    }

    private void debug(String message, Object arg1, Object arg2, Object arg3) {
        if (plugin.isDebug()) {
            plugin.getLogger().info(message, arg1, arg2, arg3);
        }
    }
}
