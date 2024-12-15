package com.broll.mpnll.server;

import com.broll.mpnll.server.inbound.SetupContext;
import com.broll.mpnll.server.inbound.TcpServerSetup;
import com.broll.mpnll.server.inbound.WebsocketServerSetup;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        SetupContext context = new SetupContext(
            bossGroup,
            workerGroup,
            null,
            null,
            null
        );

        try {
            Channel tcpChannel = TcpServerSetup.init(context);
            Channel wsChannel = WebsocketServerSetup.init(context);

            System.out.println("Server started. Listening on TCP port 8080 and WebSocket port 8081.");
            // Wait for the servers to close
            tcpChannel.closeFuture().sync();
            wsChannel.closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
