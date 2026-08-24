package com.broll.mpnll.server.lobby;

import com.broll.mpnll.nt.NT_LobbyInformation;
import com.broll.mpnll.nt.NT_LobbyUpdate;
import com.google.protobuf.Any;

public class LobbyMessageBuilder {

    private Lobby lobby;

    public LobbyMessageBuilder(Lobby lobby) {
        this.lobby = lobby;
    }

    public NT_LobbyInformation lobbyInfo() {
        NT_LobbyInformation.Builder info = NT_LobbyInformation.newBuilder();
        info.setLobbyId(lobby.id);
        info.setLobbyName(lobby.name);
        info.setPlayerCount(lobby.getPlayerCount());
        info.setPlayerLimit(lobby.playerLimit);
        info.setSettings(buildSettings());
        return info.build();
    }

    public NT_LobbyUpdate lobbyUpdate() {
        return null;
    }

    private Any buildSettings() {
        if (lobby.data == null || lobby.lobbySettingsBuilder == null) {
            return Any.getDefaultInstance();
        }
        return lobby.lobbySettingsBuilder.build(lobby);
    }

}
