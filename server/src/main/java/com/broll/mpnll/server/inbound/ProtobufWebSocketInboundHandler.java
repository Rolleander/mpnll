package com.broll.mpnll.server.inbound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.buffer.ByteBuf;

import com.broll.mpnll.message.MessageRegistry;
import com.broll.mpnll.server.session.ClientSession;
import com.broll.mpnll.server.session.ClientSessionRegistry;
import com.google.protobuf.Message;

public class ProtobufWebSocketInboundHandler extends SimpleChannelInboundHandler<WebSocketFrame> implements ClientInboundHandler{

    private ClientSessionRegistry clientSessionRegistry;
    private MessageRegistry messageRegistry;
    private MessageListener messageListener;

    public ProtobufWebSocketInboundHandler(ClientSessionRegistry clientSessionRegistry, MessageRegistry messageRegistry, MessageListener messageListener) {
        this.clientSessionRegistry = clientSessionRegistry;
        this.messageRegistry = messageRegistry;
        this.messageListener = messageListener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        ByteBuf byteBuf = msg.content();
        int typeId = byteBuf.readInt();
        byte[] messageBytes = ByteBufUtils.remainingBytes(byteBuf);
        Message message = messageRegistry.parseMessage(messageBytes, typeId);
        ClientSession session = clientSessionRegistry.get(ctx);
        this.messageListener.received(session, message);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.clientSessionRegistry.register(ctx, this);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.clientSessionRegistry.remove(ctx);
    }

    @Override
    public void send(ChannelHandlerContext context, byte[] data  ) {
        ByteBuf byteBuf = context.alloc().buffer();
        byteBuf.readBytes(data);
        context.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
    }
}
