package dev.saltt.hub.database.domains;

import java.util.UUID;

public record SurvivalGamesPlayer(
        UUID matchId,
        UUID playerUuid,
        UUID teamId,          // nullable (team_uuid)
        long timeAlive,
        String causeOfDeath   // nullable
) {}