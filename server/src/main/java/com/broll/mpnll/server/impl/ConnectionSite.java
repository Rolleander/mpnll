package com.broll.mpnll.server.impl;

import com.broll.mpnll.nt.NT_ListLobbies;
import com.broll.mpnll.nt.NT_LobbyCreate;
import com.broll.mpnll.nt.NT_LobbyJoin;
import com.broll.mpnll.nt.NT_LobbyLeave;
import com.broll.mpnll.nt.NT_LobbyNoJoin;
import com.broll.mpnll.nt.NT_ReconnectCheck;
import com.broll.mpnll.nt.NT_ServerInformation;
import com.broll.mpnll.server.lobby.Lobby;
import com.broll.mpnll.server.site.NetworkSite;
import com.broll.mpnll.server.site.PackageReceiver;
import com.broll.mpnll.server.user.User;
import com.broll.mpnll.server.user.UserRegistry;
import com.broll.mpnll.server.utils.ConnectionRestriction;
import com.broll.mpnll.server.utils.RestrictionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.stream.Collectors;

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
        if (tryReconnectToLobby(list.getAuthenticationKey())) {
            return;
        }
        NT_ServerInformation.Builder serverInfo = NT_ServerInformation.newBuilder();
        serverInfo.setServerName(getServer().getName());
        serverInfo.addAllLobbies(
            getLobbyHandler().listAll().stream().filter(Lobby::isVisible).map(lobby -> lobby.nt.lobbyInfo()).collect(Collectors.toList())
        );
        respond(serverInfo.build());
    }

    @ConnectionRestriction(RestrictionType.NOT_IN_LOBBY)
    @PackageReceiver
    public void joinLobby(NT_LobbyJoin join) {
        initUserAndJoinLobby(join.getLobbyId(), join.getPlayerName(), join.getAuthenticationKey());
    }

    @ConnectionRestriction(RestrictionType.NOT_IN_LOBBY)
    @PackageReceiver
    public void reconnectCheck(NT_ReconnectCheck check) {
        if (!tryReconnectToLobby(check.getAuthenticationKey())) {
            //is a new player, cant be reconnected
            Log.warn("Reconnect check failed: user is new and cannot be reconnected!");
            sendNoJoinResponse();
        }
    }

    @ConnectionRestriction(RestrictionType.NOT_IN_LOBBY)
    @PackageReceiver
    public void createLobby(NT_LobbyCreate create) {
        if (!checkJoiningClientVersion(create.getVersion())) {
            return;
        }
        assertUserConnection(create.getPlayerName(), create.getAuthenticationKey());
        Lobby lobby = getLobbyHandler().requestLobbyCreation(getUser(), create.getLobbyName(), create.getSettings());
        if (lobby != null) {
            joinLobby(lobby);
        } else {
            Log.warn("User " + getUser() + " was not allowed to create lobby!");
            sendNoJoinResponse();
        }
    }

    @ConnectionRestriction(RestrictionType.IN_LOBBY)
    @PackageReceiver
    public void switchLobby(NT_LobbyJoin join) {
        Lobby from = getLobby();
        if (from.isLocked() && !getUser().isAllowedToLeaveLockedLobby()) {
            Log.warn("User " + getUser() + " is not allowed to switch lobby!");
            sendNoJoinResponse();
            return;
        }
        if (getLobbyHandler().getLobby(join.getLobbyId()) == from) {
            //already in the lobby, just init player
            assertUserConnection(join.getPlayerName(), join.getAuthenticationKey());
            from.updatePublisher.userJoined(getUser());
            return;
        }
        from.removeUser(getUser(), false);
        Lobby to = getLobbyHandler().getLobby(join.getLobbyId());
        if (to != null) {
            to.addUser(getUser());
        }
    }

    @ConnectionRestriction(RestrictionType.IN_LOBBY)
    @PackageReceiver
    public void leaveLobby(NT_LobbyLeave leave) {
        getLobby().removeUser(getUser(), false);
    }

    private boolean joinLobby(Lobby lobby) {
        boolean success = lobby.addUser(getUser());
        if (!success) {
            sendNoJoinResponse();
        }
        return success;
    }

    private boolean initUserAndJoinLobby(int lobbyId, String playerName, String authenticationKey) {
        assertUserConnection(playerName, authenticationKey);
        Lobby lobby = getLobbyHandler().getLobby(lobbyId);
        if (lobby != null) {
            return joinLobby(lobby);
        }
        return false;
    }

    private void assertUserConnection(String playerName, String authenticationKey) {
        if (getUser() != null) {
            return;
        }
        User user = userRegistry.getUser(authenticationKey);
        if (user == null) {
            user = new User(userRegistry.newId(), authenticationKey, getConnection());
            userRegistry.register(authenticationKey, user);
        } else if (user.isOnline()) {
            //someone else trying to steal the player session with its key?
            respond(NT_LobbyNoJoin.newBuilder().build());
            throw new RuntimeException(authenticationKey + " already belongs to " + user + " who is still active, do not allow login for " + playerName);
        }
        user.setName(playerName);
        getConnection().connectWith(user);
    }

    private boolean tryReconnectToLobby(String key) {
        User user = userRegistry.getUser(key);
        if (user != null && !user.isOnline() && user.inLobby()) {
            user.getLobby().memberTransactions.reconnected(user, getConnection());
            return true;
        }
        return false;
    }

    private boolean checkJoiningClientVersion(String clientVersion) {
        String version = getServer().getVersion();
        if (!Objects.equals(version, clientVersion)) {
            Log.warn("User {} version does not match server!", getUser());
            respond(NT_LobbyNoJoin.newBuilder()
                .setReason("Version mismatch with server: " + version).build());
            return false;
        }
        return true;
    }

    private void sendNoJoinResponse() {
        respond(NT_LobbyNoJoin.newBuilder().build());
    }
}
