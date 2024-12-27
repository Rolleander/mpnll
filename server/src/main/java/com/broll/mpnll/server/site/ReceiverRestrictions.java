package com.broll.mpnll.server.site;

import com.broll.mpnll.server.connection.ClientConnection;
import com.broll.mpnll.server.lobby.Lobby;
import com.broll.mpnll.server.user.User;
import com.broll.mpnll.server.utils.ConnectionRestriction;
import com.broll.mpnll.server.utils.RestrictionType;
import com.google.protobuf.Message;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ReceiverRestrictions {

    private final Map<Method, RestrictionType> receiverRestrictions = new HashMap<>();

    public void registerContainerRoute(SitesHandler.ObjectTargetContainer container, Class<NetworkSite> type, Method receiverMethod) {
        ConnectionRestriction packageRestriction = receiverMethod.getAnnotation(ConnectionRestriction.class);
        RestrictionType restrictionType = RestrictionType.IN_LOBBY;
        if (packageRestriction != null) {
            restrictionType = packageRestriction.value();
        }
        receiverRestrictions.put(receiverMethod, restrictionType);
    }
    
    public boolean shouldInvokeReceiver(ClientConnection connection, NetworkSite site, Method receiver, Message object) {
        RestrictionType restrictionType = receiverRestrictions.get(receiver);
        if (restrictionType == RestrictionType.NONE) {
            return true;
        }
        User user = connection.getUser();
        Lobby lobby = null;
        if (user != null) {
            lobby = user.getLobby();
        }
        switch (restrictionType) {
            case PLAYER_CONNECTED:
                return user != null;
            case IN_LOBBY:
                return lobby != null;
            case NOT_IN_LOBBY:
                return lobby == null;
            case PLAYER_NOT_CONNECTED:
                return user == null;
            case LOBBY_LOCKED:
                if (lobby != null) return lobby.isLocked();
            case LOBBY_UNLOCKED:
                if (lobby != null) return !lobby.isLocked();
        }
        return false;
    }

}
