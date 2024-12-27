package com.broll.mpnll.server.lobby;

public interface LobbyStateListener {

    void lobbyOpened(Lobby lobby);

    void lobbyClosed(Lobby lobby);

    void lobbyLocked(Lobby lobby);

    void lobbyUnlocked(Lobby lobby);
}
