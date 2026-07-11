package dev.saltt.hub.grpc;

import dev.saltt.common.api.proto.HeartbeatAck;
import dev.saltt.common.api.proto.HeartbeatServiceGrpc;
import dev.saltt.common.api.proto.SubHeartbeatMessage;
import dev.saltt.common.api.proto.LifeGameType;
import dev.saltt.common.api.proto.GameStatus;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HeartbeatServiceImpl extends HeartbeatServiceGrpc.HeartbeatServiceImplBase {

    private static final Logger LOG = Logger.getLogger(HeartbeatServiceImpl.class.getName());

    @Override
    public void heartbeat(SubHeartbeatMessage req, StreamObserver<HeartbeatAck> obs) {
        try {
            validate(req);

            String matchId       = req.getMatchId();
            LifeGameType gameType = req.getGameType();
            GameStatus status     = req.getStatus();
            List<String> players  = req.getPlayersList();   // raw uuid strings
            Instant now           = Instant.now();          // arrival time = last-seen stamp

            // ---------------------------------------------------------------
            // TODO: hand this beat to the matchmaker. Roughly:
            //
            //   1. Upsert the live status for this match/server:
            //        matchmaker.update(matchId, gameType, status, players, now);
            //      Key on match_id (or server_id if you add one to the proto — see note below).
            //      Overwrite whatever was there; the newest beat is authoritative.
            //
            //   2. Stamp `now` as this server's last-seen time. The reaper (below)
            //      uses it to evict servers that stop beating.
            //
            //   3. React to the status transition if you care about edges, e.g.
            //      WAITING -> IN_PROGRESS means "no longer joinable",
            //      ENDED    -> remove from the joinable pool entirely.
            //
            //   4. Parse player uuids if the matchmaker needs them as UUIDs:
            //        players.stream().map(UUID::fromString).toList();
            //      (do it here so a malformed uuid becomes INVALID_ARGUMENT, not a
            //       matchmaker-internal blowup — see validate()).
            // ---------------------------------------------------------------

            obs.onNext(HeartbeatAck.newBuilder().setAcknowledged(true).build());
            obs.onCompleted();

        } catch (IllegalArgumentException e) {
            obs.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage()).withCause(e).asRuntimeException());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "heartbeat handling failed for match " + req.getMatchId(), e);
            obs.onError(Status.INTERNAL
                    .withDescription("heartbeat failed").withCause(e).asRuntimeException());
        }
    }

    private static void validate(SubHeartbeatMessage req) {
        if (req.getMatchId().isEmpty())
            throw new IllegalArgumentException("match_id required");
        if (req.getGameType() == LifeGameType.UNRECOGNIZED)
            throw new IllegalArgumentException("unknown game_type");
        if (req.getStatus() == GameStatus.UNRECOGNIZED)
            throw new IllegalArgumentException("unknown status");
        // Fail fast on malformed uuids so it's INVALID_ARGUMENT, not an INTERNAL later.
        for (String p : req.getPlayersList()) {
            try { UUID.fromString(p); }
            catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("player uuid is not a valid UUID: " + p);
            }
        }
    }
}