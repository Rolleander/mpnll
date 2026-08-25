package com.broll.mpnll.client.impl;

import com.broll.mpnll.client.ClientOperation;
import com.broll.mpnll.client.ResponseException;
import com.broll.mpnll.client.async.ClientFuture;
import com.broll.mpnll.client.lobby.Lobby;
import com.broll.mpnll.client.lobby.LobbySync;
import com.broll.mpnll.nt.NT_LobbyJoined;
import com.broll.mpnll.nt.NT_LobbyNoJoin;
import com.broll.mpnll.nt.NT_LobbyReconnected;
import com.broll.mpnll.nt.NT_ReconnectCheck;

public class CheckReconnect extends ClientOperation<Lobby> {

    private String ip;

    public CheckReconnect(String ip) {
        this.ip = ip;
    }

    @Override
    protected ClientFuture<Lobby> operation() {
        return connect(ip).thenCompose(ignored -> {
            requireConnected();
            NT_ReconnectCheck message = NT_ReconnectCheck.newBuilder()
                .setAuthenticationKey(getClientAuthentication().getKey())
                .build();
            return this.<Lobby>send(message)
                .on(NT_LobbyReconnected.newBuilder(), (NT_LobbyReconnected response) ->
                    LobbySync.reconnectedLobby(getClient(), response))
                .on(NT_LobbyNoJoin.newBuilder(), (NT_LobbyNoJoin response) -> {
                    throw new ResponseException("Could not reconnect to lobby: " + response.getReason());
                })
                .execute();
        });
    }

}
