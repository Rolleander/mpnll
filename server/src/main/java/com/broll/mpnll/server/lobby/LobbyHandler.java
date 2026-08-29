package com.broll.mpnll.server.lobby;

import com.broll.mpnll.message.MessageRegistry;
import com.broll.mpnll.server.user.User;
import com.broll.mpnll.server.user.BotUser;
import com.google.protobuf.Any;
import com.google.protobuf.Message;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class LobbyHandler {

    List<LobbyUsersListener> usersListeners = new CopyOnWriteArrayList<>();
    List<LobbyStateListener> stateListeners = new CopyOnWriteArrayList<>();
    private LobbyRegistry registry = new LobbyRegistryImpl();
    private MessageRegistry messageRegistry;
    private LobbyCreationReceiver<?> lobbyCreationReceiver;
    private final AtomicInteger botIds = new AtomicInteger(-1);

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

    public void acceptLobbyCreation(LobbyCreationCallback<Any> callback) {
        lobbyCreationReceiver = new LobbyCreationReceiver<>(Any.class, callback);
    }

    public User createBot(Lobby lobby, String name, Object data) {
        return createBot(lobby, name, data, null);
    }

    public User createBot(Lobby lobby, String name, Object data, Consumer<Message> messageReceiver) {
        BotUser bot = new BotUser(botIds.getAndDecrement(), name);
        bot.setMessageReceiver(messageReceiver);
        bot.setData(data);
        return lobby.addUser(bot) ? bot : null;
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
            S parsedSettings = settingsType == Any.class
                ? (S) settings
                : messageRegistry.unpack(settings, settingsType);
            return callback.allowCreation(requester, lobby, parsedSettings);
        }
    }
}
