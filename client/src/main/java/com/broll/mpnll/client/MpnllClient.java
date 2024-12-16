package com.broll.mpnll.client;

import com.broll.mpnll.client.site.ClientSite;
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
    private MessageRegistryImpl messageRegistry = new MessageRegistryImpl();
    private List<ClientSite> sites = new ArrayList<>();

    public MpnllClient() {
        this.nativeClient = NativeClientRegistry.createClient();
    }

    public void registerMessages(Consumer<MessageRegistrySetup> registry) {
        registry.accept(messageRegistry::register);
    }

    public synchronized void open(String host) {
        this.nativeClient.open(host, new Listener());
    }

    public synchronized void close() {
        this.nativeClient.close();
    }

    public synchronized void addSite(ClientSite site) {
        this.sites.add(site);
        site.init(this);
    }

    public synchronized void clearSites() {
        this.sites.clear();
    }

    public synchronized void removeSite(ClientSite site) {
        this.sites.remove(site);
    }

    public boolean isConnected() {
        return this.nativeClient.isConnected();
    }

    public void send(Message message) {
        int type = messageRegistry.getType(message);
        nativeClient.send(MessageUtils.toMessageBytes(type, message));
    }

    private class Listener implements ClientConnectionListener {

        @Override
        public void onOpen() {
            synchronized (MpnllClient.this) {
                sites.forEach(ClientSite::onConnect);
            }
        }

        @Override
        public void onClose() {
            synchronized (MpnllClient.this) {
                sites.forEach(ClientSite::onDisconnect);
            }
        }

        @Override
        public void onError(Throwable error) {

        }

        @Override
        public void onMessage(byte[] data) {
            int type = MessageUtils.getMessageType(data);
            byte[] content = MessageUtils.getMessageContent(data);
            synchronized (MpnllClient.this) {
                try {
                    Message message = messageRegistry.parseMessage(content, type);
                    sites.forEach(it -> it.onReceive(message));
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
