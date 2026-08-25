package com.broll.mpnll.client.impl;

import com.broll.mpnll.client.site.ClientSite;
import com.broll.mpnll.client.site.MessageReceiverRegistry;

public class LobbySite extends ClientSite {

    @Override
    protected void registerReceivers(MessageReceiverRegistry registry) {

    }

    @Override
    protected boolean isInternal() {
        return true;
    }

}
