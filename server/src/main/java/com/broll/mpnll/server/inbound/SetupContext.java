package com.broll.mpnll.server.inbound;

import com.broll.mpnll.message.MessageRegistry;
import com.broll.mpnll.server.connection.ClientConnectionRegistry;

import io.netty.channel.EventLoopGroup;

public class SetupContext {

    public final EventLoopGroup bossGroup;
    public final EventLoopGroup workerGroup;
    public final ClientConnectionRegistry clientConnectionRegistry;
    public final MessageRegistry messageRegistry;
    public final MessageListener messageListener;

    public SetupContext(EventLoopGroup bossGroup, EventLoopGroup workerGroup, ClientConnectionRegistry clientConnectionRegistry, MessageRegistry messageRegistry, MessageListener messageListener) {
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.clientConnectionRegistry = clientConnectionRegistry;
        this.messageRegistry = messageRegistry;
        this.messageListener = messageListener;
    }
}
