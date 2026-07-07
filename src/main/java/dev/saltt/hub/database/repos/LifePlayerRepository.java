package dev.saltt.hub.database.repos;

import dev.saltt.common.api.types.LifePlayer;
import dev.saltt.hub.database.Json;
import dev.saltt.hub.database.Repository;
import org.jdbi.v3.core.Jdbi;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class LifePlayerRepository extends Repository<LifePlayer> {

    public LifePlayerRepository(Jdbi jdbi) {
        super(jdbi, "life_player",
                List.of("uuid"),
                List.of("uuid", "display_name", "first_join", "last_joined", "past_names", "used_ips"),
                (rs, ctx) -> new LifePlayer(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("display_name"),
                        rs.getObject("first_join", LocalDateTime.class).toInstant(ZoneOffset.UTC),
                        rs.getObject("last_joined", LocalDateTime.class).toInstant(ZoneOffset.UTC),
                        Json.readStringList(rs.getString("past_names")),
                        Json.readStringList(rs.getString("used_ips"))));
    }

    @Override protected Map<String, Object> toRow(LifePlayer p) {
        Map<String, Object> row = new HashMap<>();
        row.put("uuid", p.uuid().toString());
        row.put("display_name", p.displayName());               // may be null
        row.put("first_join", utc(p.firstJoin()));
        row.put("last_joined", utc(p.lastJoined()));
        row.put("past_names", Json.write(p.pastNames()));
        row.put("used_ips", Json.write(p.usedIps()));
        return row;
    }

    public Optional<LifePlayer> findById(UUID uuid) {
        return findOne("uuid = :uuid", Map.of("uuid", uuid.toString()));
    }

    private static LocalDateTime utc(Instant i) {
        return LocalDateTime.ofInstant(i, ZoneOffset.UTC);
    }
}