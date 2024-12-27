package com.broll.mpnll.server.inbound;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public final class TcpServerSetup {

    public static Channel init(
        SetupContext context,
        int port
    ) throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(context.bossGroup, context.workerGroup)
            .channel(NioServerSocketChannel.class)
            .option(ChannelOption.SO_BACKLOG, 128)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                        new ProtobufTcpInboundHandler(
                            context.clientConnectionRegistry,
                            context.messageRegistry,
                            context.messageListener
                        )
                    );
                }
            });
        return b.bind(port).sync().channel();
    }

}
