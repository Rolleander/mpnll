package com.broll.mpnll.server.site;


import com.broll.mpnll.server.connection.ClientConnection;
import com.broll.mpnll.server.impl.ConnectionSite;
import com.broll.mpnll.server.utils.AnnotationScanner;
import com.google.common.collect.Lists;
import com.google.protobuf.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public abstract class SitesHandler {

    /**
     * list of sites that are protected from clearing all sites
     */
    protected final static List<Class<? extends NetworkSite>> INTERNAL_SITES = Lists.newArrayList(
        ConnectionSite.class,
        LobbySite.class,
        LobbyConnectionSite.class);
    private final static Logger Log = LoggerFactory.getLogger(SitesHandler.class);
    private final static IUnknownMessageReceiver DEFAULT_UNKNOWN_MESSAGE_RECEIVER = (message) -> {
        Log.error("No receiverMethod registered for network object " + message);
    };
    protected final ReadWriteLock siteModificationLock = new ReentrantReadWriteLock();
    protected final Map<Class<NetworkSite>, NetworkSite> sites = new HashMap<>();
    protected final Map<Class, ObjectTargetContainer> siteRoutes = new HashMap<>();
    private final ReceiverRestrictions receiverRestrictions = new ReceiverRestrictions();
    private IUnknownMessageReceiver unknownMessageReceiver = DEFAULT_UNKNOWN_MESSAGE_RECEIVER;
    private SiteReceiver<NetworkSite, ClientConnection> siteReceiver = new SiteReceiver<>();

    public abstract Map<Class<NetworkSite>, NetworkSite> getSiteInstances(ClientConnection connection);

    public void setReceiver(SiteReceiver<NetworkSite, ClientConnection> siteReceiver) {
        this.siteReceiver = siteReceiver;
    }

    public void setUnknownMessageReceiver(IUnknownMessageReceiver unknownMessageReceiver) {
        this.unknownMessageReceiver = unknownMessageReceiver;
    }

    public void clear() {
        siteModificationLock.writeLock().lock();
        new ArrayList(sites.values()).stream().filter(this::isRemovableSite).forEach(site -> {
            sites.remove(site.getClass());
            removeSite((NetworkSite) site);
        });
        siteModificationLock.writeLock().unlock();
    }

    private boolean isRemovableSite(NetworkSite site) {
        return INTERNAL_SITES.stream().noneMatch(it -> it.isInstance(site));
    }

    public final void add(NetworkSite site) {
        siteModificationLock.writeLock().lock();
        putSite(site);
        initRoute(site);
        siteModificationLock.writeLock().unlock();
    }

    protected void putSite(NetworkSite site) {
        sites.put((Class<NetworkSite>) site.getClass(), site);
    }

    public final void remove(NetworkSite site) {
        siteModificationLock.writeLock().lock();
        sites.remove(site.getClass());
        removeSite(site);
        siteModificationLock.writeLock().unlock();
    }

    protected abstract void removeSite(NetworkSite site);

    public abstract void initConnection(ClientConnection connection);

    public abstract void discardConnection(ClientConnection connection);

    private void initRoute(NetworkSite site) {
        AnnotationScanner.findAnnotatedMethods(site, PackageReceiver.class).forEach(m -> {
            Class<?>[] types = m.getParameterTypes();
            if (types.length == 1) {
                Class type = types[0];
                registerRoute(type, site, m);
            } else {
                Log.error("PackageReceiver method " + m + " of object " + site + " does not have correct amount of parameters (1)");
            }
        });
    }

    public final void pass(ClientConnection connectionContext, Message sentObject) {
        siteModificationLock.readLock().lock();
        ObjectTargetContainer container = siteRoutes.get(sentObject.getClass());
        siteModificationLock.readLock().unlock();
        if (container == null) {
            unknownMessageReceiver.unknownMessage(sentObject);
            return;
        }
        Collection<NetworkSite> sites = container.getTargetInstances(connectionContext);
        sites.forEach(it -> it.receive(connectionContext, sentObject));
        container.pass(sites, connectionContext, sentObject);
    }

    private void registerRoute(Class type, NetworkSite site, Method receiverMethod) {
        ObjectTargetContainer route = siteRoutes.get(type);
        if (route == null) {
            route = createContainer();
            siteRoutes.put(type, route);
        }
        registerContainerRoute(route, (Class<NetworkSite>) site.getClass(), receiverMethod);
    }

    public final NetworkSite accessSite(ClientConnection connection, Class<NetworkSite> siteClass) {
        NetworkSite site = getSiteInstances(connection).get(siteClass);
        if (site == null) {
            throw new RuntimeException("No site instance exists for " + siteClass);
        }
        site.receive(connection, null);
        return site;
    }

    protected abstract ObjectTargetContainer createContainer();

    protected void registerContainerRoute(ObjectTargetContainer container, Class<NetworkSite> type, Method receiverMethod) {
        receiverRestrictions.registerContainerRoute(container, type, receiverMethod);
    }

    private boolean shouldInvokeReceiver(ClientConnection context, NetworkSite site, Method receiver, Message object) {
        return receiverRestrictions.shouldInvokeReceiver(context, site, receiver, object);
    }

    public abstract class ObjectTargetContainer {

        protected abstract Collection<NetworkSite> getTargetInstances(ClientConnection connectionContext);

        protected abstract Collection<Method> getTargetMethods(NetworkSite site);

        protected void pass(Collection<NetworkSite> instances, ClientConnection connectionContext, Message sentObject) {
            instances.forEach(site -> getTargetMethods(site).stream().filter(method -> shouldInvokeReceiver(connectionContext, site, method, sentObject))
                //must be collected in between, so filter conditions are fixed before they could be changed from invoked sites
                .collect(Collectors.toList()).stream()
                .forEach(method -> siteReceiver.receive(connectionContext, site, method, sentObject)));
        }
    }

}


