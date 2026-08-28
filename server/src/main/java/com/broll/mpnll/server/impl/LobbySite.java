package com.broll.mpnll.server.impl;

import com.broll.mpnll.nt.NT_ChatMessage;
import com.broll.mpnll.nt.NT_LobbyKick;
import com.broll.mpnll.nt.NT_LobbyLeave;
import com.broll.mpnll.server.lobby.Lobby;
import com.broll.mpnll.server.site.NetworkSite;
import com.broll.mpnll.server.site.PackageReceiver;
import com.broll.mpnll.server.user.User;
import com.broll.mpnll.server.utils.ConnectionRestriction;
import com.broll.mpnll.server.utils.RestrictionType;

public class LobbySite extends NetworkSite {

    @PackageReceiver
    @ConnectionRestriction(RestrictionType.IN_LOBBY)
    private void receive(NT_ChatMessage chatMessage) {
        NT_ChatMessage message = NT_ChatMessage.newBuilder(chatMessage).setFrom(getUser().getId()).build();
        getLobby().getOnlineUsers().stream().filter(it -> it != getUser()).forEach(it -> it.send(message));
    }

    @PackageReceiver
    @ConnectionRestriction(RestrictionType.LOBBY_UNLOCKED)
    private void receive(NT_LobbyKick kick) {
        Lobby lobby = getLobby();
        if (lobby.getOwner() != getUser()) {
            return;
        }
        User member = lobby.findMember(kick.getPlayer());
        if (member == null) {
            return;
        }
        lobby.removeUser(member, true);
    }

    @PackageReceiver
    @ConnectionRestriction(RestrictionType.LOBBY_UNLOCKED)
    private void receive(NT_LobbyLeave nt) {
        getLobby().removeUser(getUser(), false);
    }

    @Override
    protected boolean isInternal() {
        return true;
    }
}
