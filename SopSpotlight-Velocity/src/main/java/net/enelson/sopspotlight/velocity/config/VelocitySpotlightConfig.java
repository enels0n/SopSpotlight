package net.enelson.sopspotlight.velocity.config;

public final class VelocitySpotlightConfig {

    private final boolean debug;

    public VelocitySpotlightConfig(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
    }
}
