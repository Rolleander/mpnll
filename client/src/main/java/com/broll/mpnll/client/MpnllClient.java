package com.broll.mpnll.client;

import com.broll.mpnll.NetworkException;
import com.broll.mpnll.client.async.ClientFuture;
import com.broll.mpnll.client.async.ClientPromise;
import com.broll.mpnll.client.async.ScheduledTask;
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
import com.broll.mpnll.client.site.ClientSite;
import com.broll.mpnll.client.site.SiteHandler;
import com.broll.mpnll.message.MessageRegistryImpl;
import com.broll.mpnll.message.MessageRegistrySetup;
import com.broll.mpnll.message.MessageUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class MpnllClient {

    private static final int CONNECTION_TIMEOUT_MILLIS = 5000;

    private final NativeClient nativeClient;
    private MessageRegistryImpl messageRegistry = new MessageRegistryImpl();
    private List<ClientStatusListener> statusListeners = new ArrayList<>();
    private ClientAuthentication clientAuthentication;
    private LastConnection lastConnection;
    private String host;
    private String version = "0";
    private Lobby connectedLobby;
    private final SiteHandler siteHandler = new SiteHandler(this, this::deactivateLobby);

    public MpnllClient() {
        this(NativeClientRegistry.createClient());
    }

    public MpnllClient(NativeClient nativeClient) {
        if (nativeClient == null) {
            throw new IllegalArgumentException("nativeClient must not be null");
        }
        this.nativeClient = nativeClient;
        configureFileAccess(nativeClient.clientAuthAccess(), nativeClient.lastConnectionAccess());
    }

    public void configureFileAccess(IFileAccess authFileAccess, IFileAccess lastConnectionFileAccess) {
        this.clientAuthentication = new ClientAuthentication(authFileAccess);
        this.lastConnection = new LastConnection(lastConnectionFileAccess);
    }

    public void registerMessages(Consumer<MessageRegistrySetup> registry) {
        registry.accept(messageRegistry::register);
    }

    public synchronized void open(String host) {
        openAsync(host);
    }

    public ClientFuture<Void> openAsync(String host) {
        if (nativeClient.isConnected() && java.util.Objects.equals(this.host, host)) {
            return ClientPromise.completed(null);
        }
        if (nativeClient.isConnected()) {
            nativeClient.close();
        }
        this.host = host;
        ClientPromise<Void> connection = new ClientPromise<>();
        ScheduledTask timeout = nativeClient.schedule(CONNECTION_TIMEOUT_MILLIS, () -> {
            connection.fail(new NetworkException("Connection timed out after 5 seconds"));
            nativeClient.close();
        });
        connection.onSuccess(ignored -> timeout.cancel());
        connection.onFailure(error -> timeout.cancel());
        try {
            this.nativeClient.open(host, new Listener(connection));
        } catch (Throwable error) {
            connection.fail(error);
        }
        return connection;
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
        nativeClient.shutdown();
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

    public ClientFuture<Lobby> reconnectCheck() {
        String ip = getLastConnection().getLastConnection();
        if (ip == null) {
            return ClientPromise.completed(null);
        }
        return reconnectCheck(ip);
    }

    public ClientFuture<Lobby> reconnectCheck(String ip) {
        return runAsyncOperation(new CheckReconnect(ip), this::activateLobby);
    }

    public ClientFuture<LobbyLookup> listLobbies() {
        return listLobbies(null);
    }

    public ClientFuture<LobbyLookup> listLobbies(String ip) {
        return runAsyncOperation(new ListLobbies(ip), null)
            .thenApply(result -> {
                if (result instanceof ReconnectToLobby) {
                    activateLobby(((ReconnectToLobby) result).getLobby());
                    return new LobbyLookup("", "", Collections.emptyList());
                }
                return (LobbyLookup) result;
            });
    }

    public ClientFuture<Lobby> joinLobby(LobbyInfo lobby, String userName) {
        return runAsyncOperation(new JoinLobby(lobby, userName), this::activateLobby);
    }

    public ClientFuture<Lobby> createLobby(String userName, Object lobbySettings) {
        return runAsyncOperation(new CreateLobby(userName, lobbySettings), this::activateLobby);
    }

    private <T> ClientFuture<T> runAsyncOperation(ClientOperation<T> operation, Consumer<T> onSuccess) {
        ClientFuture<T> result = operation.run(this);
        if (onSuccess != null) {
            result.onSuccess(onSuccess);
        }
        return result;
    }

    ScheduledTask schedule(int delayMillis, Runnable action) {
        return nativeClient.schedule(delayMillis, action);
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
        left.getLobbyListeners().forEach(it -> it.closed(left));
    }

    private class Listener implements ClientConnectionListener {

        private final ClientPromise<Void> connection;

        private Listener(ClientPromise<Void> connection) {
            this.connection = connection;
        }

        @Override
        public void onOpen() {
            connection.complete(null);
            synchronized (MpnllClient.this) {
                statusListeners.forEach(ClientStatusListener::connected);
            }
        }

        @Override
        public void onClose() {
            if (!connection.isDone()) {
                connection.fail(new NetworkException("Connection closed before it was opened"));
            }
            synchronized (MpnllClient.this) {
                statusListeners.forEach(ClientStatusListener::disconnected);
            }
        }

        @Override
        public void onError(Throwable error) {
            connection.fail(error);
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
