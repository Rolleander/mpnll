package com.broll.mpnll.server.user;

public interface UserRegistry {

    int newId();

    User getUser(String key);

    void register(String key, User user);

    void unregister(String key);
}
