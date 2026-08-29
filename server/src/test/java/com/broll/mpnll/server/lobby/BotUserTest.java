package com.broll.mpnll.server.lobby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.broll.mpnll.NtLobbyMessagesRegistry;
import com.broll.mpnll.message.MessageRegistryImpl;
import com.broll.mpnll.nt.NT_ChatMessage;
import com.broll.mpnll.server.user.User;
import com.google.protobuf.Message;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BotUserTest {

    @Test
    public void botReceivesLobbyBroadcastAsProtobuf() {
        MessageRegistryImpl registry = new MessageRegistryImpl();
        NtLobbyMessagesRegistry.register(registry);
        LobbyHandler handler = new LobbyHandler(registry);
        Lobby lobby = handler.openLobby(it -> it.setName("test"));
        List<Message> received = new ArrayList<>();

        User bot = handler.createBot(lobby, "bot", null, received::add);
        received.clear(); // Ignore the lobby-join update.
        NT_ChatMessage message = NT_ChatMessage.newBuilder().setMessage("hello").build();
        lobby.sendToAll(message);

        assertEquals(1, received.size());
        assertSame(message, received.get(0));
    }
}
