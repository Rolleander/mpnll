package com.broll.mpnll.server.lobby;

import com.broll.mpnll.server.user.User;

public interface LobbyChatHandler {

    /** @return true when the message was handled and should not be broadcast. */
    boolean handle(Lobby lobby, User sender, String message);
}
