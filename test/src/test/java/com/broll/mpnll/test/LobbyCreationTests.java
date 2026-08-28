package com.broll.mpnll.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.broll.mpnll.client.MpnllClient;
import com.broll.mpnll.client.lobby.Lobby;
import com.broll.mpnll.message.MessageRegistrySetup;
import com.broll.mpnll.server.MpnllServer;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import org.junit.Test;

public class LobbyCreationTests extends MpnllIntegrationTest {

    @Override
    protected void configureServer(MpnllServer server) {
        server.registerMessages(LobbyCreationTests::registerSettingsMapping);
        server.getLobbyHandler().acceptLobbyCreation(Settings.class, (requester, lobby, settings) -> {
            if (settings != null) {
                lobby.setData(settings);
                lobby.setName(settings.name);
                lobby.setPlayerLimit(settings.maxPlayers);
            }
            return true;
        });
    }

    @Override
    protected void configureClient(MpnllClient client) {
        client.registerMessages(LobbyCreationTests::registerSettingsMapping);
    }

    @Test
    public void versionMismatchRejectsCreation() throws Exception {
        MpnllClient client = newClient("version-mismatch");
        client.setVersion("different");

        Throwable error = awaitFailure(client.createLobby("Tester", null));

        assertTrue(error.getMessage().contains("Version mismatch with server: 0"));
    }

    @Test
    public void createsLobbyAndAssignsOwner() throws Exception {
        MpnllClient client = newClient("create");

        Lobby lobby = await(client.createLobby("Tester", null));
        com.broll.mpnll.server.lobby.Lobby serverLobby =
            server.getLobbyHandler().getLobby(lobby.getLobbyId());

        assertEquals("Tester's Lobby", lobby.getName());
        assertEquals(lobby.getMyUser(), lobby.getOwner());
        assertEquals(serverLobby.getPlayerCount(), lobby.getUserCount());
        assertSame(serverLobby.getOwner(), serverLobby.findMember(lobby.getMyUserId()));
    }

    @Test
    public void createsLobbyWithSettings() throws Exception {
        Settings settings = new Settings(5, "coolLobby");
        MpnllClient client = newClient("settings");

        Lobby lobby = await(client.createLobby("Tester", settings));
        com.broll.mpnll.server.lobby.Lobby serverLobby =
            server.getLobbyHandler().getLobby(lobby.getLobbyId());
        Settings returnedSettings = (Settings) lobby.getSettings();

        assertEquals("coolLobby", lobby.getName());
        assertEquals(5, lobby.getUserLimit());
        assertEquals(settings.name, returnedSettings.name);
        assertEquals(settings.maxPlayers, returnedSettings.maxPlayers);
        assertEquals("coolLobby", serverLobby.getName());
        assertEquals(5, serverLobby.getPlayerLimit());
    }

    private static void registerSettingsMapping(MessageRegistrySetup registry) {
        registry.registerMapping(
            Settings.class,
            "google.protobuf.Struct",
            Struct.getDefaultInstance(),
            settings -> Struct.newBuilder()
                .putFields("name", Value.newBuilder().setStringValue(settings.name).build())
                .putFields("maxPlayers", Value.newBuilder().setNumberValue(settings.maxPlayers).build())
                .build(),
            message -> new Settings(
                (int) message.getFieldsOrThrow("maxPlayers").getNumberValue(),
                message.getFieldsOrThrow("name").getStringValue()
            )
        );
    }

    private static final class Settings {

        private final int maxPlayers;
        private final String name;

        private Settings(int maxPlayers, String name) {
            this.maxPlayers = maxPlayers;
            this.name = name;
        }
    }
}
