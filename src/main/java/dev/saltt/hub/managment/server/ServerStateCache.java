package dev.saltt.hub.managment.server;

import dev.saltt.common.api.proto.SubHeartbeatMessage;

import java.util.concurrent.ConcurrentHashMap;

public class ServerStateCache {

    private final ConcurrentHashMap<String, SubServer> servers = new ConcurrentHashMap<>();

    private static ServerStateCache instance;

    public ServerStateCache() {

    }

    public static ServerStateCache getInstance() {
        //null check and error throw TODO:
        return instance;
    }

    public void init() {
        //setup
        instance = this;
    }


    public void serverHeartbeated(SubHeartbeatMessage msg) {
        if(!servers.contains(msg.getMatchId())) {
            //TODO; error , server not initalised
        }
    }

    public void registerServer() {

    }


}
