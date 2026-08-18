package com.broll.mpnll.server.lobby;

import com.broll.mpnll.message.MessageRegistry;
import com.broll.mpnll.nt.NT_LobbyLock;
import com.broll.mpnll.nt.NT_LobbyReconnected;
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

    public void userJoined(User user) {
/**
 *   NT_LobbyUpdate update = new NT_LobbyUpdate();
 *         NT_LobbyJoined joined = new NT_LobbyJoined();
 *         joined.playerId = joinedPlayer.getId();
 *         fillLobbyUpdate(update);
 *         fillLobbyUpdate(joined);
 *         getActivePlayers().forEach(p -> {
 *             if (p == joinedPlayer) {
 *                 p.sendTCP(joined);
 *             } else {
 *                 p.sendTCP(update);
 *             }
 *         });
 */
    }

    void userLeft(User user) {

    }

    void memberDisconnected(User user) {

    }

    public void memberReconnected(User user) {
        lobby.sendToAll(
            NT_LobbyReconnected.newBuilder()
                .setLobbyInfo(lobby.nt.lobbyInfo())
                .setPlayerId(user.getId()).build()
        );
    }

    void updated() {

    }

}
