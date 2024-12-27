package com.broll.mpnll.server.connection;

import com.broll.mpnll.message.MessageRegistry;
import com.broll.mpnll.message.MessageRegistryImpl;
import com.broll.mpnll.server.inbound.ClientInboundHandler;
import com.broll.mpnll.server.utils.ReadWriteLockMap;

import java.util.Collection;
import java.util.Map;

import io.netty.channel.ChannelHandlerContext;

public class ClientConnectionRegistryImpl implements ClientConnectionRegistry {

    private final MessageRegistry messageRegistry;
    private final Map<ChannelHandlerContext, ClientConnection> connections = new ReadWriteLockMap<>();

    public ClientConnectionRegistryImpl(MessageRegistryImpl messageRegistry) {
        this.messageRegistry = messageRegistry;
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
        connections.get(context).inactive();
        connections.remove(context);
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
