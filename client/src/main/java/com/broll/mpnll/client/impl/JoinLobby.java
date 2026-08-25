package com.broll.mpnll.client.impl;

import com.broll.mpnll.client.ClientOperation;
import com.broll.mpnll.client.ResponseException;
import com.broll.mpnll.client.async.ClientFuture;
import com.broll.mpnll.client.lobby.Lobby;
import com.broll.mpnll.client.lobby.LobbyInfo;
import com.broll.mpnll.client.lobby.LobbySync;
import com.broll.mpnll.nt.NT_LobbyJoin;
import com.broll.mpnll.nt.NT_LobbyJoined;
import com.broll.mpnll.nt.NT_LobbyNoJoin;

public class JoinLobby extends ClientOperation<Lobby> {

    private final LobbyInfo lobby;
    private final String userName;

    public JoinLobby(LobbyInfo lobby, String userName) {
        this.lobby = lobby;
        this.userName = userName;
    }

    @Override
    protected ClientFuture<Lobby> operation() {
        requireConnected();
        NT_LobbyJoin message = NT_LobbyJoin.newBuilder()
            .setAuthenticationKey(getClientAuthentication().getKey())
            .setPlayerName(userName)
            .setLobbyId(lobby.getLobbyId())
            .build();
        return this.<Lobby>send(message)
            .on(NT_LobbyJoined.newBuilder(), (NT_LobbyJoined response) ->
                LobbySync.joinedLobby(getClient(), response))
            .on(NT_LobbyNoJoin.newBuilder(), (NT_LobbyNoJoin response) -> {
                throw new ResponseException("Could not join lobby: " + response.getReason());
            })
            .execute();
    }

}
