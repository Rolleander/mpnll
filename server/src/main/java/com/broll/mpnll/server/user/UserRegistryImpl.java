package com.broll.mpnll.server.user;

import com.broll.mpnll.server.utils.ReadWriteLockMap;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class UserRegistryImpl implements UserRegistry {

    private final AtomicInteger idCounter = new AtomicInteger();

    private final Map<String, User> userRegister = new ReadWriteLockMap<>();

    @Override
    public int newId() {
        return idCounter.getAndIncrement();
    }

    @Override
    public User getUser(String key) {
        return userRegister.get(key);
    }

    @Override
    public void register(String key, User user) {
        userRegister.put(key, user);
    }

    @Override
    public void unregister(String key) {
        userRegister.remove(key);
    }

}
