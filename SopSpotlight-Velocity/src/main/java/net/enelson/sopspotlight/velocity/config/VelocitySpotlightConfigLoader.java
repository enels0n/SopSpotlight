package net.enelson.sopspotlight.velocity.config;

import net.enelson.sopspotlight.velocity.SopSpotlightVelocityPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class VelocitySpotlightConfigLoader {

    private final SopSpotlightVelocityPlugin plugin;
    private VelocitySpotlightConfig config;

    public VelocitySpotlightConfigLoader(SopSpotlightVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    public VelocitySpotlightConfig load() {
        ensureDefaultFile();
        Path target = plugin.getDataFolder().resolve("config.yml");
        boolean debug = false;
        try {
            List<String> lines = Files.readAllLines(target, StandardCharsets.UTF_8);
            for (String rawLine : lines) {
                String line = rawLine == null ? "" : rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("debug:")) {
                    String value = line.substring("debug:".length()).trim();
                    debug = Boolean.parseBoolean(value);
                }
            }
        } catch (IOException exception) {
            plugin.getLogger().warn("Failed to read SopSpotlight Velocity config.yml: {}", exception.getMessage());
        }
        this.config = new VelocitySpotlightConfig(debug);
        return this.config;
    }

    public VelocitySpotlightConfig getConfig() {
        return config == null ? new VelocitySpotlightConfig(false) : config;
    }

    private void ensureDefaultFile() {
        Path target = plugin.getDataFolder().resolve("config.yml");
        if (Files.exists(target)) {
            return;
        }
        try {
            Files.createDirectories(plugin.getDataFolder());
            try (InputStream in = plugin.getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (in != null) {
                    Files.copy(in, target);
                }
            }
        } catch (IOException exception) {
            plugin.getLogger().warn("Failed to create default SopSpotlight Velocity config.yml: {}", exception.getMessage());
        }
    }
}
