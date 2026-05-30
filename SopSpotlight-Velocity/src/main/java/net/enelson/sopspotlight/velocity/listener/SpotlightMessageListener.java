package net.enelson.sopspotlight.velocity.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.enelson.sopspotlight.velocity.SopSpotlightVelocityPlugin;

public final class SpotlightMessageListener {

    private final SopSpotlightVelocityPlugin plugin;

    public SpotlightMessageListener(SopSpotlightVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!plugin.getChannel().equals(event.getIdentifier())) {
            return;
        }
        event.setResult(PluginMessageEvent.ForwardResult.handled());
        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }

        Player carrier = ((ServerConnection) event.getSource()).getPlayer();
        ByteArrayDataInput input = ByteStreams.newDataInput(event.getData());
        String action = input.readUTF();
        if ("broadcast".equalsIgnoreCase(action)) {
            String playerName = input.readUTF();
            String originServerId = input.readUTF();
            boolean sendAvatar = input.readBoolean();
            String senderText = input.readUTF();
            if (originServerId == null || originServerId.trim().isEmpty()) {
                originServerId = plugin.getSpotlightService().resolvePlayerServer(carrier);
            }
            plugin.getSpotlightService().forwardSpotlight(playerName, originServerId, sendAvatar, senderText);
            return;
        }
        if (!"send".equalsIgnoreCase(action)) {
            return;
        }

        String playerName = input.readUTF();
        String serverName = input.readUTF();
        if (serverName == null || serverName.trim().isEmpty()) {
            serverName = plugin.getSpotlightService().resolvePlayerServer(carrier);
        }
        plugin.getSpotlightService().broadcastSpotlight(playerName, serverName);
    }
}
