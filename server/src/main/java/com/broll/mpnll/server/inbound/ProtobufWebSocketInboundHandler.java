package com.broll.mpnll.server.inbound;

import com.broll.mpnll.message.MessageRegistry;
import com.broll.mpnll.server.connection.ClientConnection;
import com.broll.mpnll.server.connection.ClientConnectionRegistry;
import com.google.protobuf.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class ProtobufWebSocketInboundHandler extends SimpleChannelInboundHandler<WebSocketFrame> implements ClientInboundHandler {

    private static final Logger Log = LoggerFactory.getLogger(ProtobufWebSocketInboundHandler.class);

    private ClientConnectionRegistry clientConnectionRegistry;
    private MessageRegistry messageRegistry;
    private MessageListener messageListener;
    private boolean connected;

    public ProtobufWebSocketInboundHandler(ClientConnectionRegistry clientConnectionRegistry, MessageRegistry messageRegistry, MessageListener messageListener) {
        this.clientConnectionRegistry = clientConnectionRegistry;
        this.messageRegistry = messageRegistry;
        this.messageListener = messageListener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        ByteBuf byteBuf = msg.content();
        int typeId = byteBuf.readInt();
        byte[] messageBytes = ByteBufUtils.remainingBytes(byteBuf);
        Message message = messageRegistry.parseMessage(messageBytes, typeId);
        ClientConnection session = clientConnectionRegistry.get(ctx);
        this.messageListener.received(session, message);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            this.clientConnectionRegistry.register(ctx, this);
            connected = true;
            Log.info("WebSocket client connected: {}", ctx.channel().remoteAddress());
        }
        ctx.fireUserEventTriggered(event);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (connected) {
            Log.info("WebSocket client disconnected: {}", ctx.channel().remoteAddress());
            this.clientConnectionRegistry.remove(ctx);
        }
    }

    @Override
    public void send(ChannelHandlerContext context, byte[] data) {
        ByteBuf byteBuf = context.alloc().buffer();
        byteBuf.writeBytes(data);
        context.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
    }
}
