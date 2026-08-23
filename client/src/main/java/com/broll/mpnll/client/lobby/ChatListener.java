package com.broll.mpnll.client.lobby;

public interface ChatListener {
    void fromUser(String msg, User from);

    void fromSystem(String msg);
}
