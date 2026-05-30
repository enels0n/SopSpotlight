package net.enelson.sopspotlight.bukkit;

import net.enelson.sopspotlight.bukkit.command.SpotlightCommand;
import net.enelson.sopspotlight.bukkit.listener.SpotlightIncomingListener;
import net.enelson.sopspotlight.bukkit.spotlight.SpotlightFormatter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class SopSpotlightBukkitPlugin extends JavaPlugin {

    public static final String CHANNEL = "sopspotlight:main";
    private SpotlightFormatter spotlightFormatter;

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
}
