package com.broll.mpnll.client.lobby;

public interface LobbyListener {

    void lobbyUpdated(Lobby lobby);

    void userJoined(Lobby lobby, User user);

    void userLeft(Lobby lobby, User user);

    void kickedFromLobby(Lobby lobby);

    void closed(Lobby lobby);

    void disconnected(Lobby lobby);
}
