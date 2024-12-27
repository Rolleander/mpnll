package com.broll.mpnll.server.utils;

import com.broll.mpnll.server.MpnllServer;
import com.broll.mpnll.server.connection.ClientConnection;
import com.broll.mpnll.server.site.NetworkSite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class SharedField {
    private final static Logger Log = LoggerFactory.getLogger(SharedField.class);
    private final NetworkSite site;
    private final Field field;
    private final Class dataClass;
    private final ShareLevel shareLevel;
    private final String key;

    public SharedField(NetworkSite site, Field field, ShareLevel shareLevel) {
        this.site = site;
        this.field = field;
        this.dataClass = field.getClass();
        this.shareLevel = shareLevel;
        this.key = site.getClass().getName() + ":" + field.getType().getName() + ":" + field.getName();
    }

    public void inject(MpnllServer server, ClientConnection connection) {
        try {
            field.set(site, getSharedField(server, connection));
        } catch (IllegalAccessException e) {
            Log.error("Failed to set shared field {}", field, e);
        }
    }

    private Object getSharedField(MpnllServer server, ClientConnection connection) {
        SharedData sharedData = SharedData.access(shareLevel, server, connection);
        if (sharedData == null) {
            return null;
        }
        return sharedData.getOrCreate(site, dataClass, key);
    }

}
