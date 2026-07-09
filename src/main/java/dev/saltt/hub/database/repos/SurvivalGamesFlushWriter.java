package dev.saltt.hub.database.repos;

import dev.saltt.common.api.proto.PvpStatsMessage;
import dev.saltt.common.api.proto.SurvivalGamesPlayerFlushMessage;
import dev.saltt.hub.database.domains.MatchPlayerStats;
import dev.saltt.hub.database.domains.SurvivalGamesPlayer;
import org.jdbi.v3.core.Handle;

import java.util.List;
import java.util.UUID;

public final class SurvivalGamesFlushWriter {

    private final MatchPlayerStatsGameFlushRepository stats;
    private final SurvivalGamesPlayerGameFlushRepository players;

    public SurvivalGamesFlushWriter(MatchPlayerStatsGameFlushRepository stats,
                                    SurvivalGamesPlayerGameFlushRepository players) {
        this.stats = stats;
        this.players = players;
    }

    /** Runs inside the orchestrator's transaction. Parents (stats) before children (sg rows). */
    public void writeAll(Handle h, List<SurvivalGamesPlayerFlushMessage> msgs) {
        for (SurvivalGamesPlayerFlushMessage m : msgs) {
            stats.save(h, toStats(m));      // parent
            players.save(h, toPlayer(m));   // child references it
        }
    }

    private static MatchPlayerStats toStats(SurvivalGamesPlayerFlushMessage m) {
        PvpStatsMessage p = m.getPlayerStats();
        return new MatchPlayerStats(
                UUID.fromString(m.getMatchId()),
                UUID.fromString(m.getPlayerUuid()),   // outer uuid is authoritative
                p.getKills(),
                p.getAssists(),
                p.getDamageDealt(),
                p.getDamageTaken());
    }

    private static SurvivalGamesPlayer toPlayer(SurvivalGamesPlayerFlushMessage m) {
        return new SurvivalGamesPlayer(
                UUID.fromString(m.getMatchId()),
                UUID.fromString(m.getPlayerUuid()),
                emptyToUuid(m.getTeamId()),
                m.getTimeAlive(),
                emptyToNull(m.getCauseOfDeath()));
    }

    private static UUID emptyToUuid(String s) {
        return (s == null || s.isEmpty()) ? null : UUID.fromString(s);
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }
}