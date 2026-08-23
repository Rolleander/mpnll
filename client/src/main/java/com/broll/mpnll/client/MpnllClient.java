package com.broll.mpnll.client;

import com.broll.mpnll.NetworkException;
import com.broll.mpnll.client.impl.CheckReconnect;
import com.broll.mpnll.client.impl.ListLobbies;
import com.broll.mpnll.client.impl.LookupResult;
import com.broll.mpnll.client.lobby.Lobby;
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
        return runAsyncOperation(new CheckReconnect(ip));
    }

    public CompletableFuture<LookupResult> listLobbies() {
        return listLobbies(null);
    }

    public CompletableFuture<LookupResult> listLobbies(String ip) {
        return runAsyncOperation(new ListLobbies(ip));
    }

    private <T> CompletableFuture<T> runAsyncOperation(ClientOperation<T> operation) {
        return CompletableFuture.supplyAsync(() ->
                operation.run(this)
            , operationRunner);
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
