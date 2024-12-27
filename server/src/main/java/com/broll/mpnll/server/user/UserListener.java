package com.broll.mpnll.server.user;

import com.broll.mpnll.server.lobby.Lobby;

public interface UserListener {
    void joinedLobby(User user, Lobby lobby);

    void leftLobby(User user, Lobby lobby);

    void switchedLobby(User user, Lobby from, Lobby to);

    void disconnected(User user);

    void reconnected(User user);
}
