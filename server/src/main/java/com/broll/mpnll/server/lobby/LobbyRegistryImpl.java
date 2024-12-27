package com.broll.mpnll.server.lobby;

import com.broll.mpnll.server.utils.ReadWriteLockMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LobbyRegistryImpl implements LobbyRegistry {

    private final AtomicInteger idCounter = new AtomicInteger();
    private final Map<Integer, Lobby> lobbies = new ReadWriteLockMap<>();

    @Override
    public int newId() {
        return idCounter.getAndIncrement();
    }

    @Override
    public void register(Lobby lobby) {
        this.lobbies.put(lobby.id, lobby);
    }

    @Override
    public void remove(Lobby lobby) {
        this.lobbies.remove(lobby.id);
    }

    @Override
    public Lobby get(int id) {
        return this.lobbies.get(id);
    }

    @Override
    public Collection<Lobby> all() {
        return new ArrayList<>(this.lobbies.values());
    }
}
