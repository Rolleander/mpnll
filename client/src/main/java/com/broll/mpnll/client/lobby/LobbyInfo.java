package com.broll.mpnll.client.lobby;

public class LobbyInfo {

    private String name;
    private int lobbyId;
    private int userCount;
    private int userLimit;
    private Object settings;
    private String serverIp;
    
    public String getServerIp() {
        return serverIp;
    }

    void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getLobbyId() {
        return lobbyId;
    }

    void setLobbyId(int lobbyId) {
        this.lobbyId = lobbyId;
    }

    public int getUserCount() {
        return userCount;
    }

    void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public int getUserLimit() {
        return userLimit;
    }

    void setUserLimit(int userLimit) {
        this.userLimit = userLimit;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public Object getSettings() {
        return settings;
    }

    void setSettings(Object settings) {
        this.settings = settings;
    }
}
