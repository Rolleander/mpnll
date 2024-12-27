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

    }

    @Override
    public void leftLobby(User user, Lobby lobby) {

    }

    @Override
    public void switchedLobby(User user, Lobby from, Lobby to) {

    }

    @Override
    public void disconnected(User user) {
        Log.info("User " + user + " disconnected from lobby " + this);
        lobby.usersListeners.forEach(it -> it.userDisconnected(lobby, user));
        lobby.updatePublisher.memberDisconnected(user);
    }

    @Override
    public void reconnected(User user) {
        Log.info("User " + user + " reconnected to lobby " + this);
        lobby.usersListeners.forEach(it -> it.userReconnected(lobby, user));
        lobby.updatePublisher.memberReconnected(user);
    }
}
