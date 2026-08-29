package com.broll.mpnll.server.lobby;

import com.broll.mpnll.server.user.User;
import com.broll.mpnll.server.user.UserListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UserListenerForwarder implements UserListener {

    private final static Logger Log = LoggerFactory.getLogger(UserListenerForwarder.class);
    private final Lobby lobby;

    UserListenerForwarder(Lobby lobby) {
        this.lobby = lobby;
    }

    @Override
    public void joinedLobby(User user, Lobby lobby) {
        lobby.usersListeners.forEach(it -> it.userJoined(lobby, user));
    }

    @Override
    public void leftLobby(User user, Lobby lobby) {
        lobby.usersListeners.forEach(it -> it.userLeft(lobby, user));
    }

    @Override
    public void switchedLobby(User user, Lobby from, Lobby to) {
        from.usersListeners.forEach(it -> it.userLeft(lobby, user));
        from.lobbyHandler.usersListeners.forEach(it -> it.userLeft(lobby, user));
        to.usersListeners.forEach(it -> it.userJoined(lobby, user));
        to.lobbyHandler.usersListeners.forEach(it -> it.userJoined(lobby, user));
    }

    @Override
    public void disconnected(User user) {
        lobby.usersListeners.forEach(it -> it.userDisconnected(lobby, user));
        lobby.lobbyHandler.usersListeners.forEach(it -> it.userDisconnected(lobby, user));
    }

    @Override
    public void reconnected(User user) {
        lobby.usersListeners.forEach(it -> it.userReconnected(lobby, user));
        lobby.lobbyHandler.usersListeners.forEach(it -> it.userReconnected(lobby, user));
    }
}
