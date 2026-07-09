package dev.saltt.hub.grpc;

import dev.saltt.common.api.proto.FlushAck;
import dev.saltt.common.api.proto.FlushEnvelope;
import dev.saltt.common.api.proto.FlushServiceGrpc;
import dev.saltt.hub.database.FlushOrchestrator;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.sql.SQLIntegrityConstraintViolationException;

public final class FlushServiceImpl extends FlushServiceGrpc.FlushServiceImplBase {

    private final FlushOrchestrator orchestrator;

    public FlushServiceImpl(FlushOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public void flush(FlushEnvelope req, StreamObserver<FlushAck> obs) {
        try {
            validate(req);
            orchestrator.write(req);
            obs.onNext(FlushAck.newBuilder()
                    .setMatchId(req.getMatchId())
                    .setPersisted(true)
                    .build());
            obs.onCompleted();
        } catch (IllegalArgumentException e) {
            obs.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage()).withCause(e).asRuntimeException());
        } catch (Exception e) {
            Status s = isMissingParent(e)
                    ? Status.FAILED_PRECONDITION.withDescription("match or player not persisted")
                    : Status.INTERNAL.withDescription("flush failed");
            obs.onError(s.withCause(e).asRuntimeException());
        }
    }

    private static void validate(FlushEnvelope req) {
        if (req.getMatchId().isEmpty())
            throw new IllegalArgumentException("match_id required");
        if (req.getPayloadCase() == FlushEnvelope.PayloadCase.PAYLOAD_NOT_SET)
            throw new IllegalArgumentException("payload required");
    }

    private static boolean isMissingParent(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SQLIntegrityConstraintViolationException) return true;
        }
        return false;
    }
}