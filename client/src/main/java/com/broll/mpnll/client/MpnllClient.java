package com.broll.mpnll.client;

import com.broll.mpnll.NetworkException;
import com.broll.mpnll.client.persist.ClientAuthentication;
import com.broll.mpnll.client.persist.IFileAccess;
import com.broll.mpnll.client.persist.LastConnection;
import com.broll.mpnll.client.persist.TempFileAccess;
import com.broll.mpnll.client.site.ClientSite;
import com.broll.mpnll.client.site.SiteHandler;
import com.broll.mpnll.message.MessageRegistryImpl;
import com.broll.mpnll.message.MessageRegistrySetup;
import com.broll.mpnll.message.MessageUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MpnllClient {

    private final NativeClient nativeClient;
    private final SiteHandler siteHandler = new SiteHandler(this);
    private MessageRegistryImpl messageRegistry = new MessageRegistryImpl();
    private List<ClientStatusListener> statusListeners = new ArrayList<>();
    private ClientAuthentication clientAuthentication = new ClientAuthentication(new TempFileAccess("MpnllClientAuth.dat"));
    private LastConnection lastConnection = new LastConnection(new TempFileAccess("MpnllLastNetworkConnection.dat"));
    private String host;
    private String version = "0";

    public MpnllClient() {
        this.nativeClient = NativeClientRegistry.createClient();
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
