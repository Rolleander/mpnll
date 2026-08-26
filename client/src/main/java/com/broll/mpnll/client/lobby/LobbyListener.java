package com.broll.mpnll.client.lobby;

public interface LobbyListener {

    void lobbyUpdated(Lobby lobby);

    void userJoined(Lobby lobby, User user);

    void userLeft(Lobby lobby, User user);

    void closed(Lobby lobby);

}
