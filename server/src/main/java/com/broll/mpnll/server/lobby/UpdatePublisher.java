package com.broll.mpnll.server.lobby;

import com.broll.mpnll.message.MessageRegistry;
import com.broll.mpnll.nt.NT_LobbyLock;
import com.broll.mpnll.server.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdatePublisher {

    private final static Logger Log = LoggerFactory.getLogger(UpdatePublisher.class);
    private final Lobby lobby;
    private final MessageRegistry messageRegistry;

    UpdatePublisher(Lobby lobby, MessageRegistry messageRegistry) {
        this.lobby = lobby;
        this.messageRegistry = messageRegistry;
    }

    void opened() {

    }

    void closed() {

    }

    void updatedLock(boolean locked) {
        lobby.sendToAll(NT_LobbyLock.newBuilder().setLocked(locked).build());
    }

    void userKicked(User user) {

    }

    void userJoined(User user) {

    }

    void userLeft(User user) {

    }

    void memberDisconnected(User user) {

    }

    void memberReconnected(User user) {

    }

    void updated() {

    }

}
