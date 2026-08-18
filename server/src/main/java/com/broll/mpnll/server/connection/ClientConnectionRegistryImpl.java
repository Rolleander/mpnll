package com.broll.mpnll.server.connection;

import com.broll.mpnll.message.MessageRegistry;
import com.broll.mpnll.message.MessageRegistryImpl;
import com.broll.mpnll.server.inbound.ClientInboundHandler;
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

    public ClientConnectionRegistryImpl(MessageRegistryImpl messageRegistry, UserRegistry userRegistry) {
        this.messageRegistry = messageRegistry;
        this.userRegistry = userRegistry;
    }

    @Override
    public void register(ChannelHandlerContext context, ClientInboundHandler handler) {
        ClientConnection connection = new ClientConnection(
            messageRegistry,
            context,
            handler
        );
        connections.put(context, connection);
    }

    @Override
    public void remove(ChannelHandlerContext context) {
        inactiveConnection(connections.get(context));
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
