package com.broll.mpnll.client.lobby;

import com.broll.mpnll.client.MpnllClient;
import com.broll.mpnll.nt.NT_LobbyInformation;
import com.broll.mpnll.nt.NT_LobbyJoined;
import com.broll.mpnll.nt.NT_LobbyPlayerInfo;
import com.broll.mpnll.nt.NT_LobbyReconnected;
import com.broll.mpnll.nt.NT_LobbyUpdate;

import java.util.List;
import java.util.stream.Collectors;

public final class LobbySync {

    public static Lobby joinedLobby(MpnllClient client, NT_LobbyJoined joined) {
        Lobby lobby = new Lobby(client, joined.getPlayerId());
        lobby.setServerIp(client.getHost());
        syncLobby(lobby, joined.getLobbyUpdate());
        return lobby;
    }

    public static Lobby reconnectedLobby(MpnllClient client, NT_LobbyReconnected reconnected) {
        Lobby lobby = new Lobby(client, reconnected.getPlayerId());
        lobby.setServerIp(client.getHost());
        syncLobby(lobby, reconnected.getLobbyUpdate());
        return lobby;
    }

    public static void syncLobby(Lobby lobby, NT_LobbyUpdate update) {
        syncInfo(lobby, update.getLobbyInfo());
        syncUsers(lobby, update.getPlayersList());
        lobby.setOwner(lobby.getUser(update.getOwner()));
    }

    private static void syncUsers(Lobby lobby, List<NT_LobbyPlayerInfo> users) {
        users.forEach(user -> {
            User existing = lobby.getUsersMap().get(user.getId());
            if (existing == null) {
                userJoined(lobby, user);
            } else {
                syncUser(existing, user);
            }
        });
        List<Integer> currentIds = users.stream().map(NT_LobbyPlayerInfo::getId).collect(Collectors.toList());
        lobby.getUsers().stream().filter(it -> !currentIds.contains(it.getId())).forEach(it ->
            userLeft(lobby, it)
        );
    }

    private static void userJoined(Lobby lobby, NT_LobbyPlayerInfo info) {
        User user = new User(info.getId(), lobby);
        syncUser(user, info);
        lobby.getUsersMap().put(info.getId(), user);
        lobby.lobbyListeners.forEach(it -> it.userJoined(lobby, user));
    }

    private static void userLeft(Lobby lobby, User user) {
        lobby.getUsersMap().remove(user.getId());
        lobby.lobbyListeners.forEach(it -> it.userLeft(lobby, user));
    }

    private static void syncUser(User user, NT_LobbyPlayerInfo info) {
        user.setName(info.getName());
        user.setSettings(info.getSettings());
        user.setBot(info.getBot());
    }

    public static void syncInfo(LobbyInfo lobby, NT_LobbyInformation info) {
        syncInfo(lobby, info, null);
    }

    public static void syncInfo(LobbyInfo lobby, NT_LobbyInformation info, String ip) {
        if (ip != null) {
            lobby.setServerIp(ip);
        }
        lobby.setName(info.getLobbyName());
        lobby.setLobbyId(info.getLobbyId());
        lobby.setUserCount(info.getPlayerCount());
        lobby.setUserLimit(info.getPlayerLimit());
        lobby.setSettings(info.getSettings());
    }

}
