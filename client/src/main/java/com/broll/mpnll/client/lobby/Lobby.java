package com.broll.mpnll.client.lobby;

import com.broll.mpnll.NetworkException;
import com.broll.mpnll.client.MpnllClient;
import com.broll.mpnll.nt.NT_ChatMessage;
import com.broll.mpnll.nt.NT_LobbyLeave;
import com.google.protobuf.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Lobby extends LobbyInfo {

    List<LobbyListener> lobbyListeners = new ArrayList<>();
    List<ChatListener> chatListeners = new ArrayList<>();
    private int myUserId;
    private Map<Integer, User> users = new HashMap<>();
    private MpnllClient client;
    private User owner;

    public Lobby(MpnllClient client, int myUserId) {
        this.client = client;
        this.myUserId = myUserId;
    }

    public void addLobbyListener(LobbyListener listener) {
        this.lobbyListeners.add(listener);
    }

    public void removeLobbyListener(LobbyListener listener) {
        this.lobbyListeners.remove(listener);
    }

    public void addChatListener(ChatListener listener) {
        this.chatListeners.add(listener);
    }

    public void removeChatListener(ChatListener listener) {
        this.chatListeners.remove(listener);
    }

    public List<LobbyListener> getLobbyListeners() {
        return lobbyListeners;
    }

    public List<ChatListener> getChatListeners() {
        return chatListeners;
    }

    private void assureConnected() {
        if (!client.isConnected()) {
            throw new NetworkException("Cannot send to unconnected lobby");
        }
    }

    public void sendChat(String message) {
        send(NT_ChatMessage.newBuilder().setMessage(message).build());
    }

    public void send(Message message) {
        assureConnected();
        client.send(message);
    }

    public void leave() {
        send(NT_LobbyLeave.newBuilder().build());
    }

    public Optional<User> getUser(String name) {
        return getUsers().stream().filter(p -> Objects.equals(name, p.getName())).findFirst();
    }

    public User getUser(int id) {
        return getUsers().stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    public User getMyUser() {
        return getUser(getMyUserId());
    }

    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    public boolean isFull() {
        return getUserCount() >= getUserLimit();
    }

    public User getOwner() {
        return owner;
    }

    void setOwner(User owner) {
        this.owner = owner;
    }

    public int getMyUserId() {
        return myUserId;
    }

    Map<Integer, User> getUsersMap() {
        return users;
    }

}
