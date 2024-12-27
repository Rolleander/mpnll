package com.broll.mpnll.server.user;

import com.broll.mpnll.nt.NT_LobbyPlayerInfo;
import com.broll.mpnll.server.connection.ClientConnection;
import com.broll.mpnll.server.lobby.Lobby;
import com.broll.mpnll.server.utils.SharedData;
import com.google.protobuf.Any;
import com.google.protobuf.Message;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class User {

    private final int id;
    private String name;
    private boolean online = true;
    private ClientConnection connection;

    private Lobby lobby;
    private String authenticationKey;

    private List<UserListener> listeners = new CopyOnWriteArrayList<>();
    private Object data;
    private SharedData sharedData = new SharedData();

    private boolean allowedToLeaveLockedLobby = false;

    public User(int id, String authenticationKey, ClientConnection connection) {
        this.id = id;
        this.authenticationKey = authenticationKey;
        this.connection = connection;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void send(Message message) {
        if (!online) return;
        connection.send(message);
    }

    public void send(byte[] data) {
        if (!online) return;
        connection.send(data);
    }

    public boolean isBot() {
        return false;
    }

    public void addListener(UserListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(UserListener listener) {
        this.listeners.remove(listener);
    }

    public List<UserListener> getListeners() {
        return listeners;
    }

    String getAuthenticationKey() {
        return authenticationKey;
    }

    public boolean inLobby() {
        return lobby != null;
    }

    public Lobby getLobby() {
        return lobby;
    }

    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }

    public void connect(ClientConnection connection) {
        this.connection = connection;
        if (!this.online) {
            this.listeners.forEach(it -> it.reconnected(this));
        }
        this.online = true;
    }

    public void disconnect() {
        this.online = false;
        this.connection = null;
        this.listeners.forEach(it -> it.disconnected(this));
    }

    public boolean isAllowedToLeaveLockedLobby() {
        return allowedToLeaveLockedLobby;
    }

    public void setAllowedToLeaveLockedLobby(boolean allowedToLeaveLockedLobby) {
        this.allowedToLeaveLockedLobby = allowedToLeaveLockedLobby;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOnline() {
        return online;
    }

    public ClientConnection getConnection() {
        return connection;
    }

    public SharedData getSharedData() {
        return sharedData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public NT_LobbyPlayerInfo nt() {
        return NT_LobbyPlayerInfo.newBuilder().setId(getId()).setName(getName())
            .setSettings(buildSettings()).build();
    }

    private Any buildSettings() {
        if (data == null || lobby == null || lobby.getUserSettingsBuilder() == null) {
            return Any.getDefaultInstance();
        }
        return lobby.getUserSettingsBuilder().build(this);
    }

    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", name='" + name + '\'' +
            '}';
    }

}
