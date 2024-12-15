package com.broll.mpnll.server.inbound;

import com.broll.mpnll.message.MessageRegistry;
import com.broll.mpnll.server.session.ClientSessionRegistry;

import io.netty.channel.EventLoopGroup;

public class SetupContext {

    public final EventLoopGroup bossGroup;
    public final EventLoopGroup workerGroup;
    public final ClientSessionRegistry clientSessionRegistry;
    public final MessageRegistry messageRegistry;
    public final MessageListener messageListener;

    public SetupContext(EventLoopGroup bossGroup, EventLoopGroup workerGroup, ClientSessionRegistry clientSessionRegistry, MessageRegistry messageRegistry, MessageListener messageListener) {
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.clientSessionRegistry = clientSessionRegistry;
        this.messageRegistry = messageRegistry;
        this.messageListener = messageListener;
    }
}
