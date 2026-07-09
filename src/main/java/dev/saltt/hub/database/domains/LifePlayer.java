package dev.saltt.hub.database.domains;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LifePlayer(
        UUID uuid,
        String displayName,
        Instant firstJoin,
        Instant lastJoined,
        List<String> pastNames,
        List<String> usedIps
) {
    public static LifePlayer create(UUID uuid, String name, String ip, Instant when) {
        List<String> ips = (ip == null || ip.isBlank()) ? List.of() : List.of(ip);
        return new LifePlayer(uuid, name, when, when, List.of(), ips);
    }
}