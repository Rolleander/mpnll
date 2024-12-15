package com.broll.mpnll.server.inbound;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;

import io.netty.channel.ChannelHandlerContext;

public interface ClientInboundHandler {

    void send(ChannelHandlerContext context, byte[] data);

}
