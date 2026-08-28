package com.broll.mpnll.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.broll.mpnll.client.MpnllClient;
import com.broll.mpnll.client.impl.LobbyLookup;
import com.broll.mpnll.server.lobby.Lobby;
import com.broll.mpnll.server.user.User;

import org.junit.Test;

public class LobbyConnectionTests extends MpnllIntegrationTest {

    @Test
    public void connectsMultipleClients() throws Exception {
        Lobby lobby = openLobby("TestLobby");

        joinLobby(newClient("Peter"), "Peter", lobby);
        joinLobby(newClient("Pan"), "Pan", lobby);

        assertEquals(2, lobby.getActiveUsers().size());
    }

    @Test
    public void disconnectRemovesUserFromUnlockedLobby() throws Exception {
        Lobby lobby = openLobby("TestLobby");
        joinLobby(newClient("Peter"), "Peter", lobby);
        MpnllClient pan = newClient("Pan");
        joinLobby(pan, "Pan", lobby);

        pan.shutdown();

        awaitCondition(() -> lobby.getActiveUsers().size() == 1);
        assertEquals("Peter", lobby.getActiveUsers().iterator().next().getName());
    }

    @Test
    public void transfersUserBetweenLobbies() throws Exception {
        Lobby first = openLobby("FirstLobby");
        Lobby second = openLobby("SecondLobby");
        MpnllClient peter = newClient("Peter");
        joinLobby(peter, "Peter", first);
        User serverPeter = first.getActiveUsers().iterator().next();

        joinLobby(peter, "Peter", second);

        assertTrue(first.isClosed());
        assertEquals(1, server.getLobbyHandler().listAll().size());
        assertEquals(1, second.getActiveUsers().size());
        assertSame(serverPeter, second.getActiveUsers().iterator().next());
    }

    @Test
    public void joiningSameLobbyDoesNotDuplicateUser() throws Exception {
        Lobby lobby = openLobby("TestLobby");
        MpnllClient peter = newClient("Peter");
        joinLobby(peter, "Peter", lobby);

        joinLobby(peter, "Peter", lobby);

        assertEquals(1, lobby.getActiveUsers().size());
    }

    @Test
    public void kicksUser() throws Exception {
        Lobby lobby = openLobby("TestLobby");
        joinLobby(newClient("Peter"), "Peter", lobby);
        joinLobby(newClient("Hans"), "Hans", lobby);
        User peter = lobby.getActiveUsers().stream()
            .filter(user -> "Peter".equals(user.getName()))
            .findFirst()
            .orElseThrow(AssertionError::new);

        assertTrue(lobby.removeUser(peter, true));

        assertEquals(1, lobby.getActiveUsers().size());
    }

    @Test
    public void lockedLobbyIsHiddenUntilUnlocked() throws Exception {
        Lobby lobby = openLobby("TestLobby");
        joinLobby(newClient("Peter"), "Peter", lobby);
        lobby.lock();
        MpnllClient pan = newClient("Pan");

        LobbyLookup lockedLookup = await(pan.listLobbies());
        assertFalse(lockedLookup.getLobbies().stream()
            .anyMatch(candidate -> candidate.getLobbyId() == lobby.getId()));

        lobby.unlock();
        joinLobby(pan, "Pan", lobby);
        assertEquals(2, lobby.getActiveUsers().size());
    }

    @Test
    public void fullLobbyRejectsAdditionalUser() throws Exception {
        Lobby lobby = openLobby("TestLobby");
        lobby.setPlayerLimit(1);
        joinLobby(newClient("Peter"), "Peter", lobby);
        MpnllClient pan = newClient("Pan");

        Throwable error = awaitFailure(pan.joinLobby(findLobby(pan, lobby.getId()), "Pan"));

        assertTrue(error.getMessage().contains("Could not join lobby"));
        assertEquals(1, lobby.getActiveUsers().size());
    }

    @Test
    public void versionMismatchRejectsLobbyListing() throws Exception {
        openLobby("TestLobby");
        MpnllClient client = newClient("version-mismatch");
        client.setVersion("different");

        Throwable error = awaitFailure(client.listLobbies());

        assertTrue(error.getMessage().contains("Version mismatch with server: 0"));
    }

    @Test
    public void reconnectsOfflineUserToLockedLobby() throws Exception {
        Lobby lobby = openLobby("TestLobby");
        MpnllClient peter = newClient("Peter");
        joinLobby(peter, "Peter", lobby);
        lobby.lock();

        peter.shutdown();
        awaitCondition(() -> !lobby.getActiveUsers().iterator().next().isOnline());

        MpnllClient hans = newClient("Hans");
        Throwable error = awaitFailure(hans.reconnectCheck(serverAddress));
        assertTrue(error.getMessage().contains("Could not reconnect to lobby"));

        MpnllClient reconnectedPeter = newClient("Peter");
        com.broll.mpnll.client.lobby.Lobby clientLobby =
            await(reconnectedPeter.reconnectCheck(serverAddress));

        assertEquals(lobby.getId(), clientLobby.getLobbyId());
        assertEquals(1, lobby.getActiveUsers().size());
        assertTrue(lobby.getActiveUsers().iterator().next().isOnline());
    }
}
