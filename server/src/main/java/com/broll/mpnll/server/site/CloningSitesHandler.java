package com.broll.mpnll.server.site;

import com.broll.mpnll.server.connection.ClientConnection;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.objenesis.strategy.StdInstantiatorStrategy;

import org.apache.commons.collections4.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

public class CloningSitesHandler extends SitesHandler {

    private final static Logger Log = LoggerFactory.getLogger(CloningSitesHandler.class);
    private final Kryo kryo = new Kryo();
    private Map<ClientConnection, Map<Class<NetworkSite>, NetworkSite>> activeSites = new HashMap<>();

    public CloningSitesHandler() {
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    private synchronized <O> O clone(O o) {
        return kryo.copyShallow(o);
    }

    @Override
    public Map<Class<NetworkSite>, NetworkSite> getSiteInstances(ClientConnection connection) {
        Map<Class<NetworkSite>, NetworkSite> instances = new HashMap<>();
        siteModificationLock.readLock().lock();
        instances.putAll(activeSites.get(connection));
        siteModificationLock.readLock().unlock();
        return instances;
    }

    @Override
    protected void putSite(NetworkSite site) {
        super.putSite(site);
        activeSites.values().forEach(sites -> sites.put((Class<NetworkSite>) site.getClass(), clone(site)));
    }

    @Override
    protected void removeSite(NetworkSite site) {
        Iterator<Map.Entry<Class, ObjectTargetContainer>> iterator = siteRoutes.entrySet().iterator();
        while (iterator.hasNext()) {
            MultiTargetContainer container = (MultiTargetContainer) iterator.next().getValue();
            Iterator<Map.Entry<Class<NetworkSite>, Object>> entries = container.sites.entrySet().iterator();
            while (entries.hasNext()) {
                if (site.getClass() == entries.next().getKey()) {
                    entries.remove();
                }
            }
            if (container.sites.isEmpty()) {
                iterator.remove();
            }
        }
        activeSites.values().forEach(sites -> sites.remove(site.getClass()));
    }

    @Override
    public void initConnection(ClientConnection connection) {
        siteModificationLock.writeLock().lock();
        Map<Class<NetworkSite>, NetworkSite> clonedSites = new HashMap<>();
        sites.entrySet().forEach(entry ->
            clonedSites.put(entry.getKey(), clone(entry.getValue()))
        );
        activeSites.put(connection, clonedSites);
        siteModificationLock.writeLock().unlock();
    }

    @Override
    public void discardConnection(ClientConnection connection) {
        siteModificationLock.writeLock().lock();
        activeSites.remove(connection);
        siteModificationLock.writeLock().unlock();
    }

    @Override
    protected SitesHandler.ObjectTargetContainer createContainer() {
        return new MultiTargetContainer();
    }

    @Override
    protected void registerContainerRoute(SitesHandler.ObjectTargetContainer container, Class<NetworkSite> type, Method receiverMethod) {
        super.registerContainerRoute(container, type, receiverMethod);
        ((MultiTargetContainer) container).sites.put(type, receiverMethod);
    }

    private class MultiTargetContainer extends SitesHandler.ObjectTargetContainer {
        private MultiValueMap<Class<NetworkSite>, Method> sites = new MultiValueMap<>();

        @Override
        protected Collection<NetworkSite> getTargetInstances(ClientConnection connectionContext) {
            return sites.keySet().stream().map(siteClass -> activeSites.get(connectionContext).get(siteClass)).collect(Collectors.toList());
        }

        @Override
        protected Collection<Method> getTargetMethods(NetworkSite site) {
            return sites.getCollection(site.getClass());
        }
    }
}


