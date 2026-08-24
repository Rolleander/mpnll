package com.broll.mpnll.client;

import com.broll.mpnll.NetworkException;
import com.broll.mpnll.client.impl.CheckReconnect;
import com.broll.mpnll.client.impl.CreateLobby;
import com.broll.mpnll.client.impl.JoinLobby;
import com.broll.mpnll.client.impl.ListLobbies;
import com.broll.mpnll.client.impl.LobbyLookup;
import com.broll.mpnll.client.impl.ReconnectToLobby;
import com.broll.mpnll.client.lobby.Lobby;
import com.broll.mpnll.client.lobby.LobbyInfo;
import com.broll.mpnll.client.persist.ClientAuthentication;
import com.broll.mpnll.client.persist.IFileAccess;
import com.broll.mpnll.client.persist.LastConnection;
import com.broll.mpnll.client.persist.TempFileAccess;
import com.broll.mpnll.client.site.ClientSite;
import com.broll.mpnll.client.site.SiteHandler;
import com.broll.mpnll.message.MessageRegistryImpl;
import com.broll.mpnll.message.MessageRegistrySetup;
import com.broll.mpnll.message.MessageUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

public class MpnllClient {

    private final NativeClient nativeClient;
    private final SiteHandler siteHandler = new SiteHandler(this);
    private ExecutorService operationRunner;
    private MessageRegistryImpl messageRegistry = new MessageRegistryImpl();
    private List<ClientStatusListener> statusListeners = new ArrayList<>();
    private ClientAuthentication clientAuthentication = new ClientAuthentication(new TempFileAccess("MpnllClientAuth.dat"));
    private LastConnection lastConnection = new LastConnection(new TempFileAccess("MpnllLastNetworkConnection.dat"));
    private String host;
    private String version = "0";
    private Lobby connectedLobby;

    public MpnllClient() {
        this.nativeClient = NativeClientRegistry.createClient();
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("Client-operations").build();
        this.operationRunner = Executors.newSingleThreadExecutor(threadFactory);
    }

    public void configureFileAccess(IFileAccess authFileAccess, IFileAccess lastConnectionFileAccess) {
        this.clientAuthentication = new ClientAuthentication(authFileAccess);
        this.lastConnection = new LastConnection(lastConnectionFileAccess);
    }

    public void registerMessages(Consumer<MessageRegistrySetup> registry) {
        registry.accept(messageRegistry::register);
    }

    public synchronized void open(String host) {
        this.host = host;
        this.nativeClient.open(host, new Listener());
    }

    public synchronized void close() {
        this.nativeClient.close();
    }

    public boolean isConnected() {
        return this.nativeClient.isConnected();
    }

    public void addSite(ClientSite site) {
        this.siteHandler.addSite(site);
    }

    public void removeSite(ClientSite site) {
        this.siteHandler.removeSite(site);
    }

    public void clearSites() {
        this.siteHandler.clearSites();
    }

    public void addStatusListener(ClientStatusListener listener) {
        this.statusListeners.add(listener);
    }

    public void removeStatusListener(ClientStatusListener listener) {
        this.statusListeners.remove(listener);
    }

    public void send(Message message) {
        int type = messageRegistry.getType(message);
        nativeClient.send(MessageUtils.toMessageBytes(type, message));
    }

    public void shutdown() {
        close();
        operationRunner.shutdown();
    }

    public ClientAuthentication getClientAuthentication() {
        return clientAuthentication;
    }

    public LastConnection getLastConnection() {
        return lastConnection;
    }

    public String getHost() {
        return host;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public CompletableFuture<Lobby> reconnectCheck() {
        String ip = getLastConnection().getLastConnection();
        if (ip == null) {
            return CompletableFuture.completedFuture(null);
        }
        return reconnectCheck(ip);
    }

    public CompletableFuture<Lobby> reconnectCheck(String ip) {
        return runAsyncOperation(new CheckReconnect(ip), this::activateLobby);
    }

    public CompletableFuture<LobbyLookup> listLobbies() {
        return listLobbies(null);
    }

    public CompletableFuture<LobbyLookup> listLobbies(String ip) {
        return runAsyncOperation(new ListLobbies(ip), null)
            .thenApply(result -> {
                if (result instanceof ReconnectToLobby) {
                    activateLobby(((ReconnectToLobby) result).getLobby());
                    return new LobbyLookup("", "", Collections.emptyList());
                }
                return (LobbyLookup) result;
            });
    }

    public CompletableFuture<Lobby> joinLobby(LobbyInfo lobby, String userName) {
        return runAsyncOperation(new JoinLobby(lobby, userName), this::activateLobby);
    }

    public CompletableFuture<Lobby> createLobby(String userName, Object lobbySettings) {
        return runAsyncOperation(new CreateLobby(userName, lobbySettings), this::activateLobby);
    }

    private <T> CompletableFuture<T> runAsyncOperation(ClientOperation<T> operation, Consumer<T> onSuccess) {
        return CompletableFuture.supplyAsync(() ->
                operation.run(this)
            , operationRunner).thenApply(it -> {
            if (onSuccess != null) {
                onSuccess.accept(it);
            }
            return it;
        });
    }

    public Lobby getConnectedLobby() {
        return connectedLobby;
    }

    private void activateLobby(Lobby lobby) {
        this.connectedLobby = lobby;
        statusListeners.forEach(it -> it.joinedLobby(lobby));
    }

    private void deactivateLobby() {
        Lobby left = this.connectedLobby;
        this.connectedLobby = null;
        statusListeners.forEach(it -> it.leftLobby(left));
    }

    private class Listener implements ClientConnectionListener {

        @Override
        public void onOpen() {
            synchronized (MpnllClient.this) {
                statusListeners.forEach(ClientStatusListener::connected);
            }
        }

        @Override
        public void onClose() {
            synchronized (MpnllClient.this) {
                statusListeners.forEach(ClientStatusListener::disconnected);
            }
        }

        @Override
        public void onError(Throwable error) {
            synchronized (MpnllClient.this) {
                statusListeners.forEach(it -> it.error(error));
            }
        }

        @Override
        public void onMessage(byte[] data) {
            int type = MessageUtils.getMessageType(data);
            byte[] content = MessageUtils.getMessageContent(data);
            synchronized (MpnllClient.this) {
                try {
                    Message message = messageRegistry.parseMessage(content, type);
                    siteHandler.pass(message);
                } catch (InvalidProtocolBufferException e) {
                    throw new NetworkException(e);
                }
            }
        }
    }
}
