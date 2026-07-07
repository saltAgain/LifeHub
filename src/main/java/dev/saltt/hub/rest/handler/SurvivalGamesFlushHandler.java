package dev.saltt.hub.rest.handler;

import dev.saltt.common.api.types.SurvivalGamesMatch;
import dev.saltt.common.api.types.base.PvPStats;
import dev.saltt.hub.database.repos.SurvivalGamesMatchRepository;
import dev.saltt.hub.rest.ValidationException;
import dev.saltt.hub.rest.dto.MatchFlushRequest;
import dev.saltt.hub.rest.utils.JsonHandler;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class SurvivalGamesFlushHandler extends JsonHandler<MatchFlushRequest> {

    private final SurvivalGamesMatchRepository matchRepo;

    public SurvivalGamesFlushHandler(String token, SurvivalGamesMatchRepository matchRepo) {
        super(MatchFlushRequest.class, token);
        this.matchRepo = matchRepo;
    }

    @Override
    protected Object process(MatchFlushRequest req) throws ValidationException {
        if (req.matchId() == null) throw new ValidationException("matchId is required");
        if (req.startTime() == null) throw new ValidationException("startTime is required");
        if (req.players() == null || req.players().isEmpty())
            throw new ValidationException("players must be a non-empty array");

        UUID matchId = parseUuid(req.matchId(), "matchId");

        LocalTime startTime;
        try {
            startTime = LocalTime.parse(req.startTime());
        } catch (DateTimeParseException e) {
            throw new ValidationException("startTime is not a valid time: " + req.startTime());
        }

        List<PvPStats> stats = new ArrayList<>(req.players().size());
        List<UUID> roster = new ArrayList<>(req.players().size());
        Set<UUID> seen = new HashSet<>();

        for (var p : req.players()) {
            UUID uuid = parseUuid(p.uuid(), "player uuid");
            if (!seen.add(uuid)) throw new ValidationException("duplicate player: " + uuid);
            if (p.kills() < 0 || p.assists() < 0 || p.damageDealt() < 0 || p.damageTaken() < 0)
                throw new ValidationException("stats cannot be negative for player " + uuid);

            stats.add(PvPStats.fromStorage(uuid, matchId,
                    p.kills(), p.assists(), p.damageDealt(), p.damageTaken()));
            roster.add(uuid);
        }

        SurvivalGamesMatch match =
                SurvivalGamesMatch.fromStorage(matchId, roster, startTime, stats);

        matchRepo.save(match);   // single transaction, idempotent upsert

        return new FlushResponse(matchId.toString(), stats.size());
    }

    private static UUID parseUuid(String value, String field) throws ValidationException {
        if (value == null) throw new ValidationException(field + " is required");
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(field + " is not a valid UUID: " + value);
        }
    }

    public record FlushResponse(String matchId, int playersWritten) {}
}