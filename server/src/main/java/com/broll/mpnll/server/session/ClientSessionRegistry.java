package com.broll.mpnll.server.session;

import com.broll.mpnll.server.inbound.ClientInboundHandler;

import io.netty.channel.ChannelHandlerContext;

public interface ClientSessionRegistry {

    void register(ChannelHandlerContext context, ClientInboundHandler handler);

    void remove(ChannelHandlerContext context);

    ClientSession get(ChannelHandlerContext context);

}
