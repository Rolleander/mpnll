package com.broll.mpnll.server.lobby;

import com.broll.mpnll.server.user.User;

public interface LobbyUsersListener {

    void userJoined(Lobby lobby, User user);

    void userLeft(Lobby lobby, User user);

    void userDisconnected(Lobby lobby, User user);

    void userReconnected(Lobby lobby, User user);

}
