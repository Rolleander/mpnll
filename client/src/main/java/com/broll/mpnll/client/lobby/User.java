package com.broll.mpnll.client.lobby;

public class User {

    private int id;
    private String name;
    private Object settings;
    private Lobby lobby;
    private boolean bot;
    private boolean me;

    User(int id, Lobby lobby) {
        super();
        this.id = id;
        this.lobby = lobby;
    }

    public void leaveLobby() {
        if (lobby == null) {
            return;
        }
        lobby.leave();
    }

    public Lobby getLobby() {
        return lobby;
    }

    public boolean isBot() {
        return bot;
    }

    void setBot(boolean bot) {
        this.bot = bot;
    }

    public int getId() {
        return id;
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

    public boolean isMe() {
        return me;
    }

    void setMe(boolean me) {
        this.me = me;
    }
}
