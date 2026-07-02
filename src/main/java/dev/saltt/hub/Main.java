package dev.saltt.hub;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class Main extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static Main instance;

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    public static Main getInstance() {
        return instance;
    }

    @Override
    protected void setup() {
        LOGGER.at(Level.INFO).log("[Template] Setting up...");

        LOGGER.at(Level.INFO).log("[Template] Setup complete!");
    }

    @Override
    protected void start() {
        LOGGER.at(Level.INFO).log("[Template] Started!");
    }

    @Override
    protected void shutdown() {
        LOGGER.at(Level.INFO).log("[Template] Shutting down...");
        instance = null;
    }
}