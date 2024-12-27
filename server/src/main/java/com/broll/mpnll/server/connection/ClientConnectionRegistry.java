package com.broll.mpnll.server.connection;

import com.broll.mpnll.server.inbound.ClientInboundHandler;

import java.util.Collection;

import io.netty.channel.ChannelHandlerContext;

public interface ClientConnectionRegistry {

    void register(ChannelHandlerContext context, ClientInboundHandler handler);

    void remove(ChannelHandlerContext context);

    ClientConnection get(ChannelHandlerContext context);

    Collection<ClientConnection> all();
}
