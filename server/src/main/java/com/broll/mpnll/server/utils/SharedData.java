package com.broll.mpnll.server.utils;

import com.broll.mpnll.server.MpnllServer;
import com.broll.mpnll.server.connection.ClientConnection;
import com.broll.mpnll.server.lobby.Lobby;
import com.broll.mpnll.server.site.NetworkSite;
import com.broll.mpnll.server.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class SharedData {

    private final static Logger Log = LoggerFactory.getLogger(SharedData.class);
    private Map<String, Object> data = new ReadWriteLockMap<>();

    public static SharedData access(ShareLevel shareLevel, MpnllServer server, ClientConnection connection) {
        User user = connection.getUser();
        Lobby lobby = null;
        if (user != null) {
            lobby = user.getLobby();
        }
        switch (shareLevel) {
            case LOBBY:
                if (lobby != null) {
                    return lobby.getSharedData();
                }
                return null;
            case USER:
                if (user != null) {
                    return user.getSharedData();
                }
                return null;
            case SERVER:
                return server.getSharedData();
        }
        throw new RuntimeException("unknown sharelevel " + shareLevel);
    }

    public Object getOrCreate(NetworkSite site, Class dataClass, String key) {
        Object object = data.get(key);
        if (object == null) {
            object = instantiateClass(dataClass, site);
            data.put(key, object);
        }
        return object;
    }

    private Object instantiateClass(Class dataClass, NetworkSite site) {
        try {
            if (dataClass.isMemberClass()) {
                //inner class default constructor with outer object
                Constructor constructor = dataClass.getDeclaredConstructors()[0];
                constructor.setAccessible(true);
                return constructor.newInstance(site);
            } else {
                //default constructor
                return dataClass.newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            Log.error("Failed to init shared field data " + dataClass, e);
        }
        return null;
    }
}
