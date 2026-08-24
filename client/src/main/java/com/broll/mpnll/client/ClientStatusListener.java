package com.broll.mpnll.client;

import com.broll.mpnll.client.lobby.Lobby;

public interface ClientStatusListener {

    void connected();

    void disconnected();

    void joinedLobby(Lobby lobby);

    void leftLobby(Lobby lobby);

    void error(Throwable error);
}
