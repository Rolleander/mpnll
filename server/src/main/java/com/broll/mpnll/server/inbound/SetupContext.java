package com.broll.mpnll.server.inbound;

import com.broll.mpnll.server.ProtobufMessageRegistry;
import com.broll.mpnll.server.session.ClientSessionRegistry;

import io.netty.channel.EventLoopGroup;

public class SetupContext {

    public final EventLoopGroup bossGroup;
    public final EventLoopGroup workerGroup;
    public final ClientSessionRegistry clientSessionRegistry;
    public final ProtobufMessageRegistry protobufMessageRegistry;
    public final MessageListener messageListener;

    public SetupContext(EventLoopGroup bossGroup, EventLoopGroup workerGroup, ClientSessionRegistry clientSessionRegistry, ProtobufMessageRegistry protobufMessageRegistry, MessageListener messageListener) {
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.clientSessionRegistry = clientSessionRegistry;
        this.protobufMessageRegistry = protobufMessageRegistry;
        this.messageListener = messageListener;
    }
}
