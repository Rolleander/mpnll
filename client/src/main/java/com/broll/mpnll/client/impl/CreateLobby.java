package com.broll.mpnll.client.impl;

import com.broll.mpnll.client.ClientOperation;
import com.broll.mpnll.client.ResponseException;
import com.broll.mpnll.client.lobby.Lobby;
import com.broll.mpnll.client.lobby.LobbySync;
import com.broll.mpnll.nt.NT_LobbyCreate;
import com.broll.mpnll.nt.NT_LobbyJoined;
import com.broll.mpnll.nt.NT_LobbyNoJoin;

public class CreateLobby extends ClientOperation<Lobby> {

    private final String userName;
    private final Object settings;

    public CreateLobby(String userName, Object settings) {
        this.userName = userName;
        this.settings = settings;
    }

    @Override
    protected Lobby operation() {
        requireConnected();
        NT_LobbyCreate message = NT_LobbyCreate.newBuilder()
            .setAuthenticationKey(getClientAuthentication().getKey())
            .setPlayerName(userName)
            .setLobbyName(userName + "'s Lobby")
            .setVersion(getClientVersion())
            // TODO   .setSettings((Any) settings)
            .build();
        return this.<Lobby>send(message)
            .on(NT_LobbyJoined.newBuilder(), (NT_LobbyJoined response) ->
                LobbySync.joinedLobby(getClient(), response))
            .on(NT_LobbyNoJoin.newBuilder(), (NT_LobbyNoJoin response) -> {
                throw new ResponseException("Could not create lobby: " + response.getReason());
            })
            .awaitResponse();
    }

}
