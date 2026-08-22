package com.broll.mpnll.client.site;

import com.broll.mpnll.client.MpnllClient;
import com.broll.mpnll.client.impl.LobbySite;
import com.google.common.collect.Lists;
import com.google.protobuf.Message;

import java.util.ArrayList;
import java.util.List;

public class SiteHandler {

    /**
     * list of sites that are protected from clearing all sites
     */
    private final static List<Class<? extends ClientSite>> INTERNAL_SITES = Lists.newArrayList(LobbySite.class);
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
        this.sites.removeIf(it -> INTERNAL_SITES.stream().noneMatch(site -> site.isInstance(it)));
    }

    public synchronized void removeSite(ClientSite site) {
        this.sites.remove(site);
    }

    public void pass(Message message) {
        sites.forEach(it -> it.onReceive(message));
    }

}
