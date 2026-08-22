package com.broll.mpnll.client.site;

import com.broll.mpnll.client.MpnllClient;
import com.google.protobuf.Message;

public abstract class ClientSite {

    private MpnllClient client;

    protected abstract void registerReceivers(MessageReceiverRegistry registry);

    public void init(MpnllClient client) {
        this.client = client;
    }

    public void onReceive(Message message) {

    }

    protected void send(Message message) {
        client.send(message);
    }

}
