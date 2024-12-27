package com.broll.mpnll;

import com.broll.mpnll.message.MessageRegistrySetup;
import com.broll.mpnll.nt.NT_ChatMessage;
import com.broll.mpnll.nt.NT_ListLobbies;
import com.broll.mpnll.nt.NT_LobbyClosed;
import com.broll.mpnll.nt.NT_LobbyCreate;
import com.broll.mpnll.nt.NT_LobbyInformation;
import com.broll.mpnll.nt.NT_LobbyJoin;
import com.broll.mpnll.nt.NT_LobbyJoined;
import com.broll.mpnll.nt.NT_LobbyKick;
import com.broll.mpnll.nt.NT_LobbyKicked;
import com.broll.mpnll.nt.NT_LobbyLeave;
import com.broll.mpnll.nt.NT_LobbyLock;
import com.broll.mpnll.nt.NT_LobbyNoJoin;
import com.broll.mpnll.nt.NT_LobbyPlayerInfo;
import com.broll.mpnll.nt.NT_LobbyReconnected;
import com.broll.mpnll.nt.NT_LobbyUpdate;
import com.broll.mpnll.nt.NT_ReconnectCheck;
import com.broll.mpnll.nt.NT_ServerInformation;

public final class NtLobbyMessagesRegistry {

    public static void register(MessageRegistrySetup setup) {
        setup.register(NT_ChatMessage.newBuilder());
        setup.register(NT_ListLobbies.newBuilder());
        setup.register(NT_LobbyClosed.newBuilder());
        setup.register(NT_LobbyCreate.newBuilder());
        setup.register(NT_LobbyInformation.newBuilder());
        setup.register(NT_LobbyJoin.newBuilder());
        setup.register(NT_LobbyJoined.newBuilder());
        setup.register(NT_LobbyKick.newBuilder());
        setup.register(NT_LobbyKicked.newBuilder());
        setup.register(NT_LobbyLeave.newBuilder());
        setup.register(NT_LobbyLock.newBuilder());
        setup.register(NT_LobbyNoJoin.newBuilder());
        setup.register(NT_LobbyPlayerInfo.newBuilder());
        setup.register(NT_LobbyReconnected.newBuilder());
        setup.register(NT_LobbyUpdate.newBuilder());
        setup.register(NT_ReconnectCheck.newBuilder());
        setup.register(NT_ServerInformation.newBuilder());
    }
}
