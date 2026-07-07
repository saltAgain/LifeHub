package dev.saltt.hub.database.repos;

import dev.saltt.common.api.types.SurvivalGamesMatch;

import dev.saltt.common.api.types.base.PvPStats;
import org.jdbi.v3.core.Jdbi;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class SurvivalGamesMatchRepository {

    private final Jdbi jdbi;
    private final PvpStatsRepository pvpStats;

    public SurvivalGamesMatchRepository(Jdbi jdbi, PvpStatsRepository pvpStats) {
        this.jdbi = jdbi;
        this.pvpStats = pvpStats;
    }

    public void save(SurvivalGamesMatch match) {
        jdbi.useTransaction(h -> {
            h.createUpdate("""
                    INSERT INTO game_match (match_id, game_type, start_time)
                    VALUES (:id, :type, :start)
                    ON DUPLICATE KEY UPDATE start_time = VALUES(start_time)
                    """)
                    .bind("id", match.matchId().toString())
                    .bind("type", match.gameType().name())
                    .bind("start", match.startTime())          // LocalTime binds natively
                    .execute();

            for (PvPStats s : match.pvpStats()) {
                pvpStats.save(h, s);                        // same tx, generic upsert
            }
        });
    }

    public Optional<SurvivalGamesMatch> findById(UUID matchId) {
        return jdbi.withHandle(h -> {
            Optional<LocalTime> start = h.createQuery(
                            "SELECT start_time FROM game_match WHERE match_id = :id AND game_type = :type")
                    .bind("id", matchId.toString())
                    .bind("type", LifeGameType.SURVIVAL_GAMES.name())
                    .map((rs, ctx) -> rs.getObject("start_time", LocalTime.class))
                    .findOne();

            if (start.isEmpty()) return Optional.<SurvivalGamesMatch>empty();

            List<PvPStats> stats = pvpStats.findByMatch(matchId);
            List<UUID> players = stats.stream().map(PvPStats::getUuid).distinct().toList();

            return Optional.of(SurvivalGamesMatch.fromStorage(matchId, players, start.get(), stats));
        });
    }

    /** FK ON DELETE CASCADE removes the pvp_stats rows automatically. */
    public void deleteById(UUID matchId) {
        jdbi.useHandle(h -> h.createUpdate("DELETE FROM game_match WHERE match_id = :id")
                .bind("id", matchId.toString()).execute());
    }
}