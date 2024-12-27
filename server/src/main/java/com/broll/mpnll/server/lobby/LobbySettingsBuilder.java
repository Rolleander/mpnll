package com.broll.mpnll.server.lobby;

import com.google.protobuf.Any;

public interface LobbySettingsBuilder {

    Any build(Lobby lobby);
}
