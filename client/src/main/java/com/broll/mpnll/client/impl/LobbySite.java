package com.broll.mpnll.client.impl;

import com.broll.mpnll.client.lobby.Lobby;
import com.broll.mpnll.client.lobby.LobbySync;
import com.broll.mpnll.client.site.ClientSite;
import com.broll.mpnll.client.site.MessageReceiverRegistry;
import com.broll.mpnll.nt.NT_ChatMessage;
import com.broll.mpnll.nt.NT_LobbyClosed;
import com.broll.mpnll.nt.NT_LobbyKicked;
import com.broll.mpnll.nt.NT_LobbyLock;
import com.broll.mpnll.nt.NT_LobbyUpdate;

public class LobbySite extends ClientSite {

    private final Runnable deactivateLobbyCallback;

    public LobbySite(Runnable deactivateLobbyCallback) {
        this.deactivateLobbyCallback = deactivateLobbyCallback;
    }
    
    @Override
    protected void registerReceivers(MessageReceiverRegistry registry) {
        registry.connect(NT_LobbyUpdate.class, this::update);
        registry.connect(NT_ChatMessage.class, this::chat);
        registry.connect(NT_LobbyKicked.class, this::kicked);
        registry.connect(NT_LobbyClosed.class, this::closed);
        registry.connect(NT_LobbyLock.class, this::lock);
    }

    public void update(NT_LobbyUpdate message) {
        Lobby lobby = getLobby();
        if (lobby == null) {
            return;
        }
        LobbySync.syncLobby(lobby, message);
    }

    public void chat(NT_ChatMessage message) {
        Lobby lobby = getLobby();
        if (lobby == null) {
            return;
        }
        getLobby().getChatListeners().forEach(it -> {
            if (message.getFrom() == -1) {
                it.fromSystem(message.getMessage());
            } else {
                it.fromUser(message.getMessage(), lobby.getUser(message.getFrom()));
            }
        });
    }

    public void kicked(NT_LobbyKicked message) {
        getClient().getLastConnection().clear();
        deactivateLobbyCallback.run();
    }

    public void closed(NT_LobbyClosed message) {
        getClient().getLastConnection().clear();
        deactivateLobbyCallback.run();
    }

    public void lock(NT_LobbyLock message) {
        if (message.getLocked()) {
            getClient().getLastConnection().setLastConnection(getClient().getHost());
        } else {
            getClient().getLastConnection().clear();
        }
    }

    @Override
    protected boolean isInternal() {
        return true;
    }

}
