package com.broll.mpnll.client.site;

import com.broll.mpnll.client.MpnllClient;
import com.google.protobuf.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ClientSite {

    private MpnllClient client;
    private final Map<Class<?>, Consumer<Message>> receivers = new HashMap<>();

    protected abstract void registerReceivers(MessageReceiverRegistry registry);

    public void init(MpnllClient client) {
        this.client = client;
        registerReceivers(new MessageReceiverRegistry() {
            @Override
            public <T extends Message> void connect(Class<?> messageType, Consumer<T> receiver) {
                receivers.put(messageType, (Consumer<Message>) receiver);
            }
        });
    }

    public void onReceive(Message message) {
        Consumer<Message> receiver = receivers.get(message.getClass());
        if (receiver != null) {
            receiver.accept(message);
        }
    }

    protected void send(Message message) {
        client.send(message);
    }

    protected boolean isInternal() {
        return false;
    }

}
