package com.broll.mpnll.server.lobby;

import com.broll.mpnll.message.MessageRegistry;
import com.broll.mpnll.server.user.User;
import com.google.protobuf.Any;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class LobbyHandler {

    List<LobbyUsersListener> usersListeners = new CopyOnWriteArrayList<>();
    List<LobbyStateListener> stateListeners = new CopyOnWriteArrayList<>();
    private LobbyRegistry registry = new LobbyRegistryImpl();
    private MessageRegistry messageRegistry;
    private LobbyCreationReceiver<?> lobbyCreationReceiver;

    public LobbyHandler(MessageRegistry messageRegistry) {
        this.messageRegistry = messageRegistry;
    }

    public void addUsersListener(LobbyUsersListener listener) {
        this.usersListeners.add(listener);
    }

    public void removeUsersListener(LobbyUsersListener listener) {
        this.usersListeners.remove(listener);
    }

    public void addStateListener(LobbyStateListener listener) {
        this.stateListeners.add(listener);
    }

    public void removeStateListener(LobbyStateListener listener) {
        this.stateListeners.remove(listener);
    }

    public <S> void acceptLobbyCreation(Class<S> settingsType, LobbyCreationCallback<S> callback) {
        lobbyCreationReceiver = new LobbyCreationReceiver<>(settingsType, callback);
    }

    public Lobby requestLobbyCreation(User requester, String lobbyName, Any settings) {
        Lobby lobby = new Lobby(this, messageRegistry);
        lobby.id = registry.newId();
        lobby.name = lobbyName;
        if (lobbyCreationReceiver != null) {
            if (lobbyCreationReceiver.allowCreation(requester, lobby, settings)) {
                openLobby(lobby);
                return lobby;
            }
        }
        return null;
    }

    public Lobby openLobby(Consumer<Lobby> configure) {
        Lobby lobby = new Lobby(this, messageRegistry);
        lobby.id = registry.newId();
        configure.accept(lobby);
        openLobby(lobby);
        return lobby;
    }

    private void openLobby(Lobby lobby) {
        registry.register(lobby);
        lobby.stateListeners.forEach(it -> it.lobbyOpened(lobby));
        stateListeners.forEach(it -> it.lobbyOpened(lobby));
    }

    public void closeLobby(Lobby lobby) {
        lobby.closed = true;
        lobby.hidden = true;
        registry.remove(lobby);
        lobby.stateListeners.forEach(it -> it.lobbyClosed(lobby));
        stateListeners.forEach(it -> it.lobbyClosed(lobby));
        lobby.updatePublisher.closed();
        lobby.members.clear();
    }

    public Lobby getLobby(int id) {
        return registry.get(id);
    }

    public void closeAll() {
        registry.all().forEach(this::closeLobby);
    }

    public Collection<Lobby> listAll() {
        return registry.all();
    }

    private class LobbyCreationReceiver<S> {
        private Class<S> settingsType;
        private LobbyCreationCallback<S> callback;

        LobbyCreationReceiver(Class<S> settingsType, LobbyCreationCallback<S> callback) {
            this.settingsType = settingsType;
            this.callback = callback;
        }

        boolean allowCreation(User requester, Lobby lobby, Any settings) {
            S parsedSettings = messageRegistry.unpack(settings, settingsType);
            return callback.allowCreation(requester, lobby, parsedSettings);
        }
    }
}
