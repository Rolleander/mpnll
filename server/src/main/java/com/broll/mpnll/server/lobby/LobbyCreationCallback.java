package com.broll.mpnll.server.lobby;

import com.broll.mpnll.server.user.User;
import com.google.protobuf.Message;

public interface LobbyCreationCallback<S extends Message> {

    boolean allowCreation(User requester, Lobby lobby, S settings);

}
