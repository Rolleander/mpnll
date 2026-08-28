package com.broll.mpnll.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

final class NettyTcpChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final int MAX_FRAME_LENGTH = 16 * 1024 * 1024;

    private final NettyTcpConnection connection;

    NettyTcpChannelInitializer(NettyTcpConnection connection) {
        this.connection = connection;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        channel.pipeline().addLast(
            new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, 0, 4, 0, 4),
            new LengthFieldPrepender(4),
            new NettyTcpInboundHandler(connection)
        );
    }
}
