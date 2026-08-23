package com.broll.mpnll.client.impl;

import com.broll.mpnll.client.lobby.LobbyInfo;

import java.util.List;

public class LobbyLookup implements LookupResult {

    private String serverName;
    private String serverIp;
    private List<LobbyInfo> lobbies;

    public LobbyLookup(String serverName, String serverIp, List<LobbyInfo> lobbies) {
        this.serverName = serverName;
        this.serverIp = serverIp;
        this.lobbies = lobbies;
    }

    public String getServerIp() {
        return serverIp;
    }

    public String getServerName() {
        return serverName;
    }

    public List<LobbyInfo> getLobbies() {
        return lobbies;
    }
}
