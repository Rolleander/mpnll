package com.broll.mpnll.client.site;

import com.broll.mpnll.client.MpnllClient;
import com.google.protobuf.Message;

import java.util.ArrayList;
import java.util.List;

public class SiteHandler {

    private final MpnllClient client;
    private final List<ClientSite> sites = new ArrayList<>();

    public SiteHandler(MpnllClient client) {
        this.client = client;
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

    public void pass(Message message) {
        new ArrayList<>(sites).forEach(site -> site.onReceive(message));
    }

}
