package dev.saltt.hub.database;

import com.google.protobuf.Timestamp;
import dev.saltt.common.api.proto.FlushEnvelope;
import dev.saltt.common.api.proto.SurvivalGamesPayload;
import dev.saltt.hub.database.repos.SurvivalGamesFlushWriter;
import org.jdbi.v3.core.Jdbi;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class FlushOrchestrator {

    private final Jdbi jdbi;
    private final SurvivalGamesFlushWriter sgWriter;

    public FlushOrchestrator(Jdbi jdbi, SurvivalGamesFlushWriter sgWriter) {
        this.jdbi = jdbi;
        this.sgWriter = sgWriter;
    }

    public void write(FlushEnvelope env) {
        jdbi.useTransaction(h -> {
            h.createUpdate("""
                    INSERT INTO life_match
                        (match_id, started_at, ended_at, server_id, map_id, region)
                    VALUES
                        (:match_id, :started_at, :ended_at, :server_id, :map_id, :region)
                    ON DUPLICATE KEY UPDATE ended_at = VALUES(ended_at)
                    """)
                    .bind("match_id",   env.getMatchId())
                    .bind("started_at", toLdt(env.getStartTimeNano()))
                    .bind("ended_at",   env.hasEndTimeNano() ? toLdt(env.getEndTimeNano()) : null)
                    .bind("server_id",  env.getServerId())
                    .bind("map_id",     env.getMapName())
                    .bind("region",     env.getRegion())
                    .execute();

            switch (env.getPayloadCase()) {
                case SURVIVAL_GAMES_PAYLOAD -> {
                    SurvivalGamesPayload p = env.getSurvivalGamesPayload();
                    sgWriter.writeAll(h, p.getPlayersList());
                }
                case PAYLOAD_NOT_SET ->
                        throw new IllegalStateException("payload not set (validate should have caught this)");
            }
        });
    }

    private static LocalDateTime toLdt(Timestamp ts) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()), ZoneOffset.UTC);
    }
}