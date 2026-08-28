package com.broll.mpnll.test;

import static org.junit.Assert.fail;

import com.broll.mpnll.NtLobbyMessagesRegistry;
import com.broll.mpnll.client.MpnllClient;
import com.broll.mpnll.client.MpnllTcpClient;
import com.broll.mpnll.client.async.ClientFuture;
import com.broll.mpnll.client.impl.LobbyLookup;
import com.broll.mpnll.client.lobby.LobbyInfo;
import com.broll.mpnll.server.MpnllServer;
import com.broll.mpnll.server.lobby.Lobby;

import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

public abstract class MpnllIntegrationTest {

    private static final long TIMEOUT_SECONDS = 5;

    protected MpnllServer server;
    protected String serverAddress;
    private final List<MpnllClient> clients = new ArrayList<>();

    @Before
    public void startServer() throws Exception {
        server = new MpnllServer();
        server.setName("TestServer");
        server.registerMessages(NtLobbyMessagesRegistry::register);
        configureServer(server);
        server.open(0, 0);
        serverAddress = "tcp://127.0.0.1:" + server.getTcpPort();
    }

    @After
    public void stopServer() {
        clients.forEach(MpnllClient::shutdown);
        if (server != null) {
            server.close();
        }
    }

    protected void configureServer(MpnllServer server) {
    }

    protected void configureClient(MpnllClient client) {
    }

    protected MpnllClient newClient(String identity) throws Exception {
        MpnllClient client = new MpnllClient(new MpnllTcpClient());
        client.configureFileAccess(
            new InMemoryFileAccess("test-auth-" + identity),
            new InMemoryFileAccess()
        );
        client.registerMessages(NtLobbyMessagesRegistry::register);
        configureClient(client);
        clients.add(client);
        await(client.openAsync(serverAddress));
        return client;
    }

    protected Lobby openLobby(String name) {
        return server.getLobbyHandler().openLobby(lobby -> lobby.setName(name));
    }

    protected com.broll.mpnll.client.lobby.Lobby joinLobby(
        MpnllClient client,
        String userName,
        Lobby serverLobby
    ) throws Exception {
        LobbyInfo lobby = findLobby(client, serverLobby.getId());
        return await(client.joinLobby(lobby, userName));
    }

    protected LobbyInfo findLobby(MpnllClient client, int lobbyId) throws Exception {
        LobbyLookup lookup = await(client.listLobbies());
        return lookup.getLobbies().stream()
            .filter(lobby -> lobby.getLobbyId() == lobbyId)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Lobby not found: " + lobbyId));
    }

    protected <T> T await(ClientFuture<T> source) throws Exception {
        return toCompletableFuture(source).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    protected Throwable awaitFailure(ClientFuture<?> source) throws Exception {
        try {
            toCompletableFuture(source).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            fail("Expected asynchronous operation to fail");
            return null;
        } catch (ExecutionException error) {
            return error.getCause();
        }
    }

    protected void awaitCondition(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() >= deadline) {
                throw new TimeoutException("Condition was not met within " + TIMEOUT_SECONDS + " seconds");
            }
            Thread.sleep(10);
        }
    }

    private <T> CompletableFuture<T> toCompletableFuture(ClientFuture<T> source) {
        CompletableFuture<T> future = new CompletableFuture<>();
        source.onSuccess(future::complete);
        source.onFailure(future::completeExceptionally);
        return future;
    }
}
