package dev.saltt.hub.rest;

import com.sun.net.httpserver.HttpServer;
import dev.saltt.hub.HubConfig;
import dev.saltt.hub.database.repos.SurvivalGamesMatchRepository;
import dev.saltt.hub.rest.handler.SurvivalGamesFlushHandler;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public final class HttpApi implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(HttpApi.class.getName());

    private final HttpServer server;
    private final ExecutorService executor;

    private HttpApi(HttpServer server, ExecutorService executor) {
        this.server = server;
        this.executor = executor;
    }

    public static HttpApi start(HubConfig cfg, SurvivalGamesMatchRepository sgRepo) throws IOException {
        HttpServer server = HttpServer.create(
                new InetSocketAddress(cfg.getApiBind(), cfg.getApiPort()), 0);

        // Dedicated pool -> HTTP handling never runs on the caller/world thread.
        ExecutorService executor = Executors.newFixedThreadPool(cfg.getApiThreads(), named("LifeHub-Http"));
        server.setExecutor(executor);

        server.createContext("/v1/matches/survival-games",
                new SurvivalGamesFlushHandler(cfg.getApiToken(), sgRepo));

        server.start();
        LOG.info("LifeHub API listening on " + cfg.getApiBind() + ":" + cfg.getApiPort());
        return new HttpApi(server, executor);
    }

    @Override public void close() {
        server.stop(3);          // drain in-flight requests, up to 3s
        executor.shutdown();     // custom executor isn't stopped by stop(), do it here
        LOG.info("LifeHub API stopped");
    }

    private static ThreadFactory named(String prefix) {
        AtomicInteger n = new AtomicInteger(1);
        return r -> {
            Thread t = new Thread(r, prefix + "-" + n.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
    }
}