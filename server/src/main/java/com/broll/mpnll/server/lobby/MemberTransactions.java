package com.broll.mpnll.server.lobby;

import com.broll.mpnll.server.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemberTransactions {

    private final static Logger Log = LoggerFactory.getLogger(MemberTransactions.class);
    private final Lobby lobby;
    private final UserListenerForwarder userListenerForwarder;

    MemberTransactions(Lobby lobby) {
        this.lobby = lobby;
        this.userListenerForwarder = new UserListenerForwarder(lobby);
    }

    public boolean addUser(User user) {
        if (lobby.isFull() || lobby.locked || lobby.closed) {
            Log.warn("Cannot add user to lobby in current state");
            return false;
        }
        if (lobby.members.contains(user)) {
            if (lobby.isActiveMember(user)) {
                Log.warn("User is already active in lobby");
                return false;
            } else {
                Log.info("Reactive user {} in lobby {}", user, lobby);
                user.setLobby(lobby);
                return true;
            }
        }
        user.setAllowedToLeaveLockedLobby(false);
        lobby.members.add(user);
        user.setLobby(lobby);
        if (lobby.owner == null) {
            autoAssignOwner();
        }
        Log.info("Added user {} to lobby {}", user, lobby);
        user.getListeners().forEach(it -> it.joinedLobby(user, lobby));
        user.addListener(userListenerForwarder);
        lobby.usersListeners.forEach(it -> it.userJoined(lobby, user));
        return true;
    }

    public boolean removeUser(User user) {
        if (!lobby.members.contains(user)) {
            Log.warn("Cannot remove user {} from lobby {} (not a member)", user, lobby);
            return false;
        }
        if (lobby.locked && !user.isAllowedToLeaveLockedLobby()) {
            Log.warn("User {} is not allowed to leave locked lobbies", user);
            return false;
        }
        user.removeListener(userListenerForwarder);
        //only set lobby to null if player is from this lobby, to prevent unsetting lobby when transfering player
        if (user.getLobby() == lobby) {
            user.setLobby(null);
        }
        if (!lobby.locked) {
            lobby.members.remove(user);
        }
        user.setLobby(null);
        if (lobby.owner == user) {
            autoAssignOwner();
        }
        Log.info("Removed user {} from lobby {}", user, lobby);
        user.getListeners().forEach(it -> it.leftLobby(user, lobby));
        lobby.usersListeners.forEach(it -> it.userLeft(lobby, user));
        checkAutoClose();
        return true;
    }

    private void autoAssignOwner() {
        if (lobby.owner == null) {
            if (!lobby.getActiveUsers().isEmpty()) {
                //find next non bot and make him owner
                lobby.getActiveUsers().stream().filter(it -> !it.isBot()).findFirst().ifPresent(player -> {
                    lobby.owner = player;
                });
            }
        } else {
            if (!lobby.getActiveUsers().contains(lobby.owner)) {
                //owner left, pick new random one
                lobby.owner = null;
                autoAssignOwner();
            }
        }
    }

    private void checkAutoClose() {
        if (lobby.autoClose && !lobby.closed && !lobby.hasNonBotMembers()) {
            lobby.close();
        }
    }
}
