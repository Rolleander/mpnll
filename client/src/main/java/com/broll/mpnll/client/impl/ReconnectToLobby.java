package com.broll.mpnll.client.impl;

import com.broll.mpnll.client.lobby.Lobby;

public class ReconnectToLobby implements LookupResult {

    private Lobby lobby;

    public ReconnectToLobby(Lobby lobby) {
        this.lobby = lobby;
    }

    public Lobby getLobby() {
        return lobby;
    }
}
