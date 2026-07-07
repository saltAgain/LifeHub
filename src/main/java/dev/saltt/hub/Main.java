package dev.saltt.hub;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;

import dev.saltt.hub.database.Database;
import dev.saltt.hub.database.repos.LifePlayerRepository;
import dev.saltt.hub.database.repos.PvpStatsRepository;
import dev.saltt.hub.database.repos.SurvivalGamesMatchRepository;
import dev.saltt.hub.rest.HttpApi;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class Main extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static Main instance;

    // MUST be created in the constructor / field initializer — loading after setup() throws.
    private final Config<HubConfig> config = this.withConfig("LifeHub", HubConfig.CODEC);

    private Database database;
    private PlayerService playerService;
    private SurvivalGamesMatchRepository matchRepo;
    private HttpApi httpApi;

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        LOGGER.atInfo().log("Hello from %s version %s",
                this.getName(), this.getManifest().getVersion().toString());
    }

    public static Main getInstance() { return instance; }
    public PlayerService players() { return playerService; }

    @Override
    protected void setup() {
        LOGGER.at(Level.INFO).log("[LifeHub] Setting up...");

        config.save();                        // create the config file on first run
        HubConfig cfg = config.get();          // read once, on the setup (single) thread

        this.database = Database.connect(cfg);    // Hikari + Flyway migrations + JDBI

        PvpStatsRepository pvpRepo = new PvpStatsRepository(database.jdbi());
        this.matchRepo = new SurvivalGamesMatchRepository(database.jdbi(), pvpRepo);
        LifePlayerRepository playerRepo = new LifePlayerRepository(database.jdbi());

        this.playerService = new PlayerService(playerRepo);

        // Player data on join / leave — DB work is offloaded inside PlayerService, never the main thread.
        getEventRegistry().registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);
        getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, this::onPlayerDisconnect);

        LOGGER.at(Level.INFO).log("[LifeHub] Setup complete");
    }

    @Override
    protected void start() {
        try {
            this.httpApi = HttpApi.start(config.get(), matchRepo);
            LOGGER.at(Level.INFO).log("[LifeHub] Flush API started");
        } catch (Exception e) {
            LOGGER.at(Level.SEVERE).log("[LifeHub] Failed to start flush API: " + e.getMessage());
        }
    }

    @Override
    protected void shutdown() {
        LOGGER.at(Level.INFO).log("[LifeHub] Shutting down...");
        if (httpApi != null) httpApi.close();
        if (playerService != null) playerService.close();
        if (database != null) database.close();
        instance = null;
    }

    private void onPlayerReady(PlayerReadyEvent event) {
        Ref<EntityStore> ref = event.getPlayerRef();
        Store<EntityStore> store = ref.getStore();

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        playerService.onJoin(playerRef.getUuid(), playerRef.getUsername(), null);   // ip: null-safe
    }

    private void onPlayerDisconnect(PlayerDisconnectEvent event) {
        Ref<EntityStore> ref = event.getPlayerRef().getReference();
        Store<EntityStore> store = ref.getStore();

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        playerService.onLeave(playerRef.getUuid());
    }
}