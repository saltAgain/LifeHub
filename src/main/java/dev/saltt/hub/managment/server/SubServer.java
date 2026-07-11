package dev.saltt.hub.managment.server;

import dev.saltt.common.api.proto.GameStatus;
import dev.saltt.common.api.proto.LifeGameType;
import dev.saltt.common.api.proto.ServerRegion;

import java.util.List;

public record SubServer(String matchId, ServerRegion region, LifeGameType gameType,
                        int maxPlayers, int minStartingPlayers, GameStatus status,
                        List<String> connectedPlayerIds, List<String> authedPlayerIds) {
}
