package com.broll.mpnll.client.impl;

import com.broll.mpnll.client.ClientOperation;
import com.broll.mpnll.client.ResponseException;
import com.broll.mpnll.client.lobby.LobbyInfo;
import com.broll.mpnll.client.lobby.LobbySync;
import com.broll.mpnll.nt.NT_ListLobbies;
import com.broll.mpnll.nt.NT_LobbyInformation;
import com.broll.mpnll.nt.NT_LobbyNoJoin;
import com.broll.mpnll.nt.NT_LobbyReconnected;
import com.broll.mpnll.nt.NT_ServerInformation;

import java.util.stream.Collectors;

public class ListLobbiesOperation extends ClientOperation<LookupResult> {

    private String ip;

    public ListLobbiesOperation() {
        this(null);
    }

    public ListLobbiesOperation(String ip) {
        this.ip = ip;
    }

    @Override
    protected void operation() {
        if (ip != null) {
            connect(ip);
        }
        requireConnected();
        NT_ListLobbies message = NT_ListLobbies.newBuilder()
            .setAuthenticationKey(getClientAuthentication().getKey())
            .setVersion(getClientVersion()).build();
        LookupResult result = this.<LookupResult>send(message)
            .on(NT_ServerInformation.newBuilder(), (NT_ServerInformation response) ->
                new LobbyLookup(
                    response.getServerName(),
                    getConnectedIp(),
                    response.getLobbiesList().stream().map(this::toLobbyInfo).collect(Collectors.toList())))
            .on(NT_LobbyReconnected.newBuilder(), (NT_LobbyReconnected response) ->
                new ReconnectToLobby(LobbySync.reconnectedLobby(getClient(), response))
            )
            .on(NT_LobbyNoJoin.newBuilder(), (NT_LobbyNoJoin response) -> {
                throw new ResponseException("Could not list lobbies: " + response.getReason());
            })
            .awaitResponse();
        complete(result);
    }

    private LobbyInfo toLobbyInfo(NT_LobbyInformation info) {
        LobbyInfo lobby = new LobbyInfo();
        LobbySync.syncInfo(lobby, info, getConnectedIp());
        return lobby;
    }
}
