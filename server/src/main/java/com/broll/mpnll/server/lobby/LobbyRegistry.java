package com.broll.mpnll.server.lobby;

import java.util.Collection;

public interface LobbyRegistry {

    int newId();

    void register(Lobby lobby);

    void remove(Lobby lobby);

    Lobby get(int id);

    Collection<Lobby> all();
}
