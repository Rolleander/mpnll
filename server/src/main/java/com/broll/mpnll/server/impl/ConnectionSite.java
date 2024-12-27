package com.broll.mpnll.server.impl;

import com.broll.mpnll.nt.NT_ListLobbies;
import com.broll.mpnll.nt.NT_LobbyNoJoin;
import com.broll.mpnll.server.site.NetworkSite;
import com.broll.mpnll.server.site.PackageReceiver;
import com.broll.mpnll.server.user.UserRegistry;
import com.broll.mpnll.server.utils.ConnectionRestriction;
import com.broll.mpnll.server.utils.RestrictionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class ConnectionSite extends NetworkSite {

    private final static Logger Log = LoggerFactory.getLogger(ConnectionSite.class);

    private final UserRegistry userRegistry;

    public ConnectionSite(UserRegistry userRegistry) {
        this.userRegistry = userRegistry;
    }

    @ConnectionRestriction(RestrictionType.NONE)
    @PackageReceiver
    public void listLobbies(NT_ListLobbies list) {
        if (!checkJoiningClientVersion(list.getVersion())) {
            return;
        }
        if (tryReconnect(list.authenticationKey)) {
            return;
        }
        NT_ServerInformation serverInfo = new NT_ServerInformation();
        serverInfo.serverName = serverName;
        serverInfo.lobbies = lobbyHandler.getLobbies().stream().filter(ServerLobby::isVisible).map(ServerLobby::getLobbyInfo).toArray(NT_LobbyInformation[]::new);
        getConnection().sendTCP(serverInfo);
    }


    private boolean checkJoiningClientVersion(String clientVersion) {
        String version = getServer().getVersion();
        if (!Objects.equals(version, clientVersion)) {
            Log.warn("User " + getUser() + " version does not match server!");
            respond(NT_LobbyNoJoin.newBuilder()
                .setReason("Version mismatch with server: " + version).build());
            return false;
        }
        return true;
    }
}
