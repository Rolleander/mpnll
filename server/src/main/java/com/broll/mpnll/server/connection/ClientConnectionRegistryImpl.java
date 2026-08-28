package com.broll.mpnll.server.connection;

import com.broll.mpnll.message.MessageRegistry;
import com.broll.mpnll.message.MessageRegistryImpl;
import com.broll.mpnll.server.inbound.ClientInboundHandler;
import com.broll.mpnll.server.site.SitesHandler;
import com.broll.mpnll.server.user.User;
import com.broll.mpnll.server.user.UserRegistry;
import com.broll.mpnll.server.utils.ReadWriteLockMap;

import java.util.Collection;
import java.util.Map;

import io.netty.channel.ChannelHandlerContext;

public class ClientConnectionRegistryImpl implements ClientConnectionRegistry {

    private final MessageRegistry messageRegistry;
    private final Map<ChannelHandlerContext, ClientConnection> connections = new ReadWriteLockMap<>();
    private final UserRegistry userRegistry;
    private final SitesHandler sitesHandler;

    public ClientConnectionRegistryImpl(
        MessageRegistryImpl messageRegistry,
        UserRegistry userRegistry,
        SitesHandler sitesHandler
    ) {
        this.messageRegistry = messageRegistry;
        this.userRegistry = userRegistry;
        this.sitesHandler = sitesHandler;
    }

    @Override
    public void register(ChannelHandlerContext context, ClientInboundHandler handler) {
        ClientConnection connection = new ClientConnection(
            messageRegistry,
            context,
            handler
        );
        connections.put(context, connection);
        sitesHandler.initConnection(connection);
    }

    @Override
    public void remove(ChannelHandlerContext context) {
        ClientConnection connection = connections.get(context);
        inactiveConnection(connection);
        sitesHandler.discardConnection(connection);
        connections.remove(context);
    }

    private void inactiveConnection(ClientConnection connection) {
        User user = connection.getUser();
        connection.removed();
        if (user != null && !user.inLobby()) {
            userRegistry.unregister(user.getAuthenticationKey());
        }
    }

    @Override
    public ClientConnection get(ChannelHandlerContext context) {
        return connections.get(context);
    }

    @Override
    public Collection<ClientConnection> all() {
        return connections.values();
    }
}
