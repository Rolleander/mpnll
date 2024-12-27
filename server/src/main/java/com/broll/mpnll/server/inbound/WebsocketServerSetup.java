package com.broll.mpnll.server.inbound;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public final class WebsocketServerSetup {

    public static Channel init(
        SetupContext context,
        int port
    ) throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(context.bossGroup, context.workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                        new HttpRequestDecoder(),
                        new HttpObjectAggregator(65536),
                        new HttpResponseEncoder(),
                        new ProtobufWebSocketInboundHandler(
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
