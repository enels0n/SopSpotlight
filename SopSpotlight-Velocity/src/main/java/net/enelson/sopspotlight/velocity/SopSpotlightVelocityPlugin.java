package net.enelson.sopspotlight.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.enelson.sopspotlight.velocity.command.SpotlightVelocityCommand;
import net.enelson.sopspotlight.velocity.config.VelocitySpotlightConfigLoader;
import net.enelson.sopspotlight.velocity.listener.SpotlightMessageListener;
import net.enelson.sopspotlight.velocity.spotlight.SpotlightService;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "sopspotlight",
        name = "SopSpotlight",
        version = "0.1.0-SNAPSHOT",
        authors = {"E_NeLsOn"},
        dependencies = {
                @Dependency(id = "skinsrestorer", optional = true)
        }
)
public final class SopSpotlightVelocityPlugin {

    private static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from("sopspotlight:main");

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataFolder;
    private VelocitySpotlightConfigLoader configLoader;
    private SpotlightService spotlightService;

    @Inject
    public SopSpotlightVelocityPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {
        this.server = server;
        this.logger = logger;
        this.dataFolder = dataFolder;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        this.configLoader = new VelocitySpotlightConfigLoader(this);
        this.configLoader.load();
        this.spotlightService = new SpotlightService(this);
        server.getChannelRegistrar().register(CHANNEL);
        server.getCommandManager().register(
                server.getCommandManager().metaBuilder("spotlight").aliases("spot").build(),
                new SpotlightVelocityCommand(this)
        );
        server.getEventManager().register(this, new SpotlightMessageListener(this));
        logger.info("SopSpotlight-Velocity initialized.");
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataFolder() {
        return dataFolder;
    }

    public MinecraftChannelIdentifier getChannel() {
        return CHANNEL;
    }

    public SpotlightService getSpotlightService() {
        return spotlightService;
    }

    public VelocitySpotlightConfigLoader getConfigLoader() {
        return configLoader;
    }

    public boolean isDebug() {
        return configLoader != null && configLoader.getConfig().isDebug();
    }

    public boolean reloadPlugin() {
        if (configLoader != null) {
            configLoader.load();
        }
        return true;
    }
}
