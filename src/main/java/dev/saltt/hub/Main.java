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
import dev.saltt.hub.database.FlushOrchestrator;
import dev.saltt.hub.database.repos.MatchPlayerStatsGameFlushRepository;
import dev.saltt.hub.database.repos.PlayerRepository;
import dev.saltt.hub.database.repos.SurvivalGamesFlushWriter;
import dev.saltt.hub.database.repos.SurvivalGamesPlayerGameFlushRepository;

import dev.saltt.hub.grpc.FlushServiceImpl;
import dev.saltt.hub.grpc.LifePlayerServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Main extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static Main instance;

    private final Config<HubConfig> config = this.withConfig("LifeHub", HubConfig.CODEC);

    private Database database;
    private PlayerService playerService;

    private FlushOrchestrator orchestrator;
    private Server grpcServer;
    private ExecutorService grpcExecutor;

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

        config.save();
        HubConfig cfg = config.get();

        this.database = Database.connect(cfg);


        var statsRepo    = new MatchPlayerStatsGameFlushRepository(database.jdbi());
        var sgRepo       = new SurvivalGamesPlayerGameFlushRepository(database.jdbi());
        var sgWriter     = new SurvivalGamesFlushWriter(statsRepo, sgRepo);
        this.orchestrator = new FlushOrchestrator(database.jdbi(), sgWriter);

        PlayerRepository playerRepo = new PlayerRepository(database.jdbi());

        getEventRegistry().registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);
        getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, this::onPlayerDisconnect);

        LOGGER.at(Level.INFO).log("[LifeHub] Setup complete");
    }

    @Override
    protected void start() {
        HubConfig cfg = config.get();
        try {
            this.grpcExecutor = Executors.newFixedThreadPool(cfg.getApiThreads());

            this.grpcServer = ServerBuilder.forPort(cfg.getApiPort())
                    .executor(grpcExecutor)
                    .addService(new FlushServiceImpl(orchestrator))
                    .build()
                    .start();

            this.grpcServer = ServerBuilder.forPort(cfg.getApiPort())
                    .executor(grpcExecutor)
                    .addService(new FlushServiceImpl(orchestrator))
                    .addService(new LifePlayerServiceImpl(playerRepo))
                    .build()
                    .start();

            LOGGER.at(Level.INFO).log("[LifeHub] Flush gRPC server listening on port " + cfg.getApiPort());
        } catch (Exception e) {
            LOGGER.at(Level.SEVERE).log("[LifeHub] Failed to start flush gRPC server: " + e.getMessage());
        }
    }


    @Override
    protected void shutdown() {
        LOGGER.at(Level.INFO).log("[LifeHub] Shutting down...");

        // Reverse order: stop accepting flushes, drain, then close the pool.
        if (grpcServer != null) {
            try {
                grpcServer.shutdown().awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                grpcServer.shutdownNow();
            }
        }
        if (grpcExecutor != null) grpcExecutor.shutdown();
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