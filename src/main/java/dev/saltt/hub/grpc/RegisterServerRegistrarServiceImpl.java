package dev.saltt.hub.grpc;

import dev.saltt.common.api.proto.RegisterServerAck;
import dev.saltt.common.api.proto.RegisterServerMessage;
import dev.saltt.common.api.proto.RegisterServerRegistrarServiceGrpc;
import dev.saltt.hub.managment.server.ServerStateCache;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.hypixel.hytale.server.npc.corecomponents.movement.BodyMotionFindBase.DebugFlags.Status;

public final class RegisterServerRegistrarServiceImpl
        extends RegisterServerRegistrarServiceGrpc.RegisterServerRegistrarServiceImplBase {

    private static final Logger LOGGER =
            Logger.getLogger(RegisterServerRegistrarServiceImpl.class.getName());

    @Override
    public void registerServer(RegisterServerMessage request,
                               StreamObserver<RegisterServerAck> responseObserver) {
        try {
            ServerStateCache.getInstance().registerServer(request);

            responseObserver.onNext(RegisterServerAck.newBuilder()
                    .setAcknowledged(true)
                    .build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[Registrar] registerServer failed", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Registration failed")
                    .asRuntimeException());
        }
    }
}