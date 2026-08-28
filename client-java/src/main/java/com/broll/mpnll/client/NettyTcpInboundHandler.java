package com.broll.mpnll.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

final class NettyTcpInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final NettyTcpConnection connection;

    NettyTcpInboundHandler(NettyTcpConnection connection) {
        this.connection = connection;
    }

    @Override
    public void channelActive(ChannelHandlerContext context) {
        if (!connection.connected(context.channel())) {
            context.close();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, ByteBuf message) {
        byte[] data = new byte[message.readableBytes()];
        message.readBytes(data);
        connection.receive(data);
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
        connection.disconnected(context.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable error) {
        connection.reportError(error);
        context.close();
    }
}
