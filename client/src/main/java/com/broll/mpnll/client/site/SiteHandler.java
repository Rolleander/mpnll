package com.broll.mpnll.client.site;

import com.broll.mpnll.client.MpnllClient;
import com.broll.mpnll.client.impl.LobbySite;
import com.google.protobuf.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SiteHandler {

    private static final Logger Log = LoggerFactory.getLogger(SiteHandler.class);

    private final MpnllClient client;
    private final List<ClientSite> sites = new ArrayList<>();
    private Consumer<Message> unknownMessageReceiver = message ->
        Log.error("No client receiver registered for network object {}", message);

    public SiteHandler(MpnllClient client, Runnable deactivateLobbyCallback) {
        this.client = client;
        addSite(new LobbySite(deactivateLobbyCallback));
    }

    public synchronized void addSite(ClientSite site) {
        this.sites.add(site);
        site.init(client);
    }

    public synchronized void clearSites() {
        this.sites.removeIf(site -> !site.isInternal());
    }

    public synchronized void removeSite(ClientSite site) {
        this.sites.remove(site);
    }

    public synchronized void setUnknownMessageReceiver(Consumer<Message> receiver) {
        this.unknownMessageReceiver = receiver;
    }

    public void pass(Message message) {
        List<ClientSite> currentSites;
        Consumer<Message> currentUnknownReceiver;
        synchronized (this) {
            currentSites = new ArrayList<>(sites);
            currentUnknownReceiver = unknownMessageReceiver;
        }
        boolean received = false;
        for (ClientSite site : currentSites) {
            if (site.receives(message.getClass())) {
                site.onReceive(message);
                received = true;
            }
        }
        if (!received) {
            currentUnknownReceiver.accept(message);
        }
    }

}
