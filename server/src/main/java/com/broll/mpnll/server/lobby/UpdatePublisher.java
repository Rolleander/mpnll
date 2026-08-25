package com.broll.mpnll.server.lobby;

import com.broll.mpnll.message.MessageRegistry;
import com.broll.mpnll.message.MessageUtils;
import com.broll.mpnll.nt.NT_LobbyJoined;
import com.broll.mpnll.nt.NT_LobbyKicked;
import com.broll.mpnll.nt.NT_LobbyLeave;
import com.broll.mpnll.nt.NT_LobbyLock;
import com.broll.mpnll.nt.NT_LobbyReconnected;
import com.broll.mpnll.nt.NT_LobbyUpdate;
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

    void closed() {
        lobby.sendToAll(NT_LobbyLeave.newBuilder().build());
    }

    void updatedLock(boolean locked) {
        lobby.sendToAll(NT_LobbyLock.newBuilder().setLocked(locked).build());
    }

    void userKicked(User user) {
        user.send(NT_LobbyKicked.newBuilder().build());
    }

    public void userJoined(User user) {
        user.send(
            NT_LobbyJoined.newBuilder()
                .setPlayerId(user.getId())
                .setLobbyUpdate(lobby.nt.lobbyUpdate()).build());
        sendUpdateExcept(user);
    }

    void userLeft(User user) {
        user.send(NT_LobbyLeave.newBuilder().build());
        sendUpdate();
    }

    void memberDisconnected(User user) {
        sendUpdate();
    }

    public void memberReconnected(User user) {
        user.send(NT_LobbyReconnected.newBuilder()
            .setLobbyUpdate(lobby.nt.lobbyUpdate())
            .setPlayerId(user.getId()).build());
        sendUpdateExcept(user);
    }

    private void sendUpdate() {
        sendUpdateExcept(null);
    }

    private void sendUpdateExcept(User exceptTo) {
        NT_LobbyUpdate update = lobby.nt.lobbyUpdate();
        byte[] data = MessageUtils.toMessageBytes(messageRegistry, update);
        lobby.getOnlineUsers().stream().filter(it -> it != exceptTo).forEach(it -> it.send(data));
    }

}
