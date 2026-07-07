package dev.saltt.hub.rest.dto;

import java.util.List;

public record MatchFlushRequest(
        String matchId,
        String startTime,
        List<PlayerStatsDto> players) {

    public record PlayerStatsDto(
            String uuid,
            int kills,
            int assists,
            long damageDealt,
            long damageTaken) {}
}