package com.broll.mpnll.server.lobby;

import com.broll.mpnll.server.user.User;
public interface LobbyCreationCallback<S> {

    boolean allowCreation(User requester, Lobby lobby, S settings);

}
