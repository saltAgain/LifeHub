package dev.saltt.hub;

import dev.saltt.common.api.types.LifePlayer;
import dev.saltt.hub.database.repos.LifePlayerRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlayerService implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(PlayerService.class.getName());

    private final LifePlayerRepository repo;
    private final ExecutorService dbExecutor;
    private final Map<UUID, LifePlayer> online = new ConcurrentHashMap<>();

    public PlayerService(LifePlayerRepository repo) {
        this.repo = repo;
        this.dbExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "LifeHub-PlayerDB");
            t.setDaemon(true);
            return t;
        });
    }

    /** Called from the event thread; DB work is offloaded. ip may be null. */
    public void onJoin(UUID uuid, String currentName, String ip) {
        dbExecutor.execute(() -> {
            try {
                LifePlayer player = repo.findById(uuid).orElseGet(() -> LifePlayer.create(uuid));
                player.recordLogin(currentName, ip);
                repo.save(player);
                online.put(uuid, player);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to persist join for " + uuid, e);
            }
        });
    }

    /** Persists any in-session mutations and evicts from the online cache. */
    public void onLeave(UUID uuid) {
        dbExecutor.execute(() -> {
            try {
                LifePlayer player = online.remove(uuid);
                if (player != null) {
                    repo.save(player);
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to persist leave for " + uuid, e);
            }
        });
    }

    public Optional<LifePlayer> getOnline(UUID uuid) {
        return Optional.ofNullable(online.get(uuid));
    }

    @Override public void close() {
        dbExecutor.shutdown();
        try {
            if (!dbExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                dbExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            dbExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}