package com.broll.mpnll.server.lobby;

import com.broll.mpnll.message.MessageRegistry;
import com.broll.mpnll.message.MessageUtils;
import com.broll.mpnll.server.user.User;
import com.broll.mpnll.server.user.UserSettingsBuilder;
import com.broll.mpnll.server.utils.SharedData;
import com.google.protobuf.Any;
import com.google.protobuf.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Lobby {

    public final static int NO_PLAYER_LIMIT = -1;
    private final static Logger Log = LoggerFactory.getLogger(Lobby.class);
    int id = -1;
    User owner;
    int playerLimit = NO_PLAYER_LIMIT;
    List<User> members = new CopyOnWriteArrayList<>();
    boolean locked = false;
    boolean hidden = false;
    boolean closed = false;
    boolean autoClose = true;
    List<LobbyUsersListener> usersListeners = new CopyOnWriteArrayList<>();
    List<LobbyStateListener> stateListeners = new CopyOnWriteArrayList<>();

    String name;
    LobbyHandler lobbyHandler;
    LobbySettingsBuilder lobbySettingsBuilder;
    UserSettingsBuilder userSettingsBuilder;
    MessageRegistry messageRegistry;
    MemberTransactions memberTransactions;
    UpdatePublisher updatePublisher;
    Object data;

    private SharedData sharedData = new SharedData();

    Lobby(LobbyHandler lobbyHandler, MessageRegistry messageRegistry) {
        this.lobbyHandler = lobbyHandler;
        this.messageRegistry = messageRegistry;
        this.memberTransactions = new MemberTransactions(this);
        this.updatePublisher = new UpdatePublisher(this, messageRegistry);
    }

    public void sendToAll(Message message) {
        byte[] data = MessageUtils.toMessageBytes(messageRegistry, message);
        getOnlineUsers().forEach(it -> it.send(data));
    }

    public synchronized void close() {
        lobbyHandler.closeLobby(this);
    }

    public synchronized void lock() {
        if (!this.locked) {
            updateLock(true);
            stateListeners.forEach(it -> it.lobbyLocked(this));
            lobbyHandler.stateListeners.forEach(it -> it.lobbyLocked(this));
        }
    }

    public synchronized void unlock() {
        if (this.locked) {
            updateLock(false);
            stateListeners.forEach(it -> it.lobbyUnlocked(this));
            lobbyHandler.stateListeners.forEach(it -> it.lobbyUnlocked(this));
        }
    }

    public synchronized void addUser(User user) {
        if (memberTransactions.addUser(user)) {
            updatePublisher.userJoined(user);
        }
    }

    public synchronized void removeUser(User user, boolean kicked) {
        if (memberTransactions.removeUser(user)) {
            if (kicked) {
                updatePublisher.userKicked(user);
            } else {
                updatePublisher.userLeft(user);
            }
        }
    }

    private void updateLock(boolean lock) {
        this.locked = lock;
        updatePublisher.updatedLock(lock);
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    public void setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
    }

    public boolean isFull() {
        if (playerLimit == NO_PLAYER_LIMIT) {
            return false;
        }
        return members.size() >= playerLimit;
    }

    boolean hasNonBotMembers() {
        return members.stream().anyMatch(it -> !it.isBot());
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

    public UserSettingsBuilder getUserSettingsBuilder() {
        return userSettingsBuilder;
    }

    public void setUserSettingsBuilder(UserSettingsBuilder userSettingsBuilder) {
        this.userSettingsBuilder = userSettingsBuilder;
    }

    public Collection<User> getAllUsers() {
        return new ArrayList<>(this.members);
    }

    public Collection<User> getActiveUsers() {
        return this.members.stream().filter(this::isActiveMember).collect(Collectors.toList());
    }

    public Collection<User> getOnlineUsers() {
        return this.members.stream().filter(it -> isActiveMember(it) && it.isOnline()).collect(Collectors.toList());
    }

    public boolean isMember(User user) {
        return members.contains(user);
    }

    public boolean isActiveMember(User user) {
        return user.getLobby() == this;
    }

    private Any buildSettings() {
        if (data == null || lobbySettingsBuilder == null) {
            return Any.getDefaultInstance();
        }
        return lobbySettingsBuilder.build(this);
    }

    @Override
    public String toString() {
        return "Lobby{" +
            "id=" + id +
            ", name='" + name + '\'' +
            '}';
    }

    public boolean isLocked() {
        return locked;
    }

    public SharedData getSharedData() {
        return sharedData;
    }
}
