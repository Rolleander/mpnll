package com.broll.mpnll.client;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class MpnllTcpClientTest {

    @Test
    public void connectsSendsAndReceivesFramedMessages() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);
        Channel server = null;
        MpnllTcpClient client = new MpnllTcpClient();
        try {
            server = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        channel.pipeline().addLast(
                            new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4),
                            new LengthFieldPrepender(4),
                            new SimpleChannelInboundHandler<ByteBuf>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext context, ByteBuf message) {
                                    byte[] data = new byte[message.readableBytes()];
                                    message.readBytes(data);
                                    context.writeAndFlush(Unpooled.wrappedBuffer(data));
                                }
                            }
                        );
                    }
                })
                .bind(0)
                .sync()
                .channel();

            CountDownLatch opened = new CountDownLatch(1);
            CountDownLatch received = new CountDownLatch(1);
            CountDownLatch closed = new CountDownLatch(1);
            AtomicReference<byte[]> receivedData = new AtomicReference<>();
            AtomicReference<Throwable> error = new AtomicReference<>();

            int port = ((java.net.InetSocketAddress) server.localAddress()).getPort();
            client.open("tcp://127.0.0.1:" + port, new ClientConnectionListener() {
                @Override
                public void onOpen() {
                    opened.countDown();
                }

                @Override
                public void onClose() {
                    closed.countDown();
                }

                @Override
                public void onError(Throwable cause) {
                    error.compareAndSet(null, cause);
                }

                @Override
                public void onMessage(byte[] data) {
                    receivedData.set(data);
                    received.countDown();
                }
            });

            assertTrue("client did not connect", opened.await(5, TimeUnit.SECONDS));
            assertTrue(client.isConnected());

            byte[] message = new byte[]{0, 0, 0, 7, 10, 20, 30};
            client.send(message);

            assertTrue("client did not receive the echoed message", received.await(5, TimeUnit.SECONDS));
            assertArrayEquals(message, receivedData.get());
            assertNull(error.get());

            client.close();
            assertTrue("client did not report closure", closed.await(5, TimeUnit.SECONDS));
        } finally {
            client.shutdown();
            if (server != null) {
                server.close().syncUninterruptibly();
            }
            workerGroup.shutdownGracefully().syncUninterruptibly();
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }
    }
}
