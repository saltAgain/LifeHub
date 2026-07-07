package dev.saltt.hub.database.repos;


import dev.saltt.common.api.types.base.PvPStats;
import dev.saltt.hub.database.Repository;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PvpStatsRepository extends Repository<PvPStats> {

    public static final RowMapper<PvPStats> MAPPER = (rs, ctx) -> PvPStats.fromStorage(
            UUID.fromString(rs.getString("uuid")),
            UUID.fromString(rs.getString("match_id")),
            rs.getInt("kills"), rs.getInt("assists"),
            rs.getLong("damage_dealt"), rs.getLong("damage_taken"));

    public PvpStatsRepository(Jdbi jdbi) {
        super(jdbi, "pvp_stats",
                List.of("match_id", "uuid"),
                List.of("match_id", "uuid", "kills", "assists", "damage_dealt", "damage_taken"),
                MAPPER);
    }

    @Override protected Map<String, Object> toRow(PvPStats s) {
        Map<String, Object> row = new HashMap<>();
        row.put("match_id", s.getMatchId().toString());
        row.put("uuid", s.getUuid().toString());
        row.put("kills", s.getKills());
        row.put("assists", s.getAssists());
        row.put("damage_dealt", s.getDamageDealt());
        row.put("damage_taken", s.getDamageTaken());
        return row;
    }

    public List<PvPStats> findByMatch(UUID matchId) {
        return find("match_id = :m", Map.of("m", matchId.toString()));
    }

    public Optional<PvPStats> findByKey(UUID matchId, UUID uuid) {
        return findOne("match_id = :m AND uuid = :u",
                Map.of("m", matchId.toString(), "u", uuid.toString()));
    }
}