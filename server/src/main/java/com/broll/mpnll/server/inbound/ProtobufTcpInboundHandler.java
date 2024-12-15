package com.broll.mpnll.server.inbound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.buffer.ByteBuf;

import com.broll.mpnll.server.ProtobufMessageRegistry;
import com.broll.mpnll.server.session.ClientSession;
import com.broll.mpnll.server.session.ClientSessionRegistry;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;

public class ProtobufTcpInboundHandler extends SimpleChannelInboundHandler<ByteBuf> implements ClientInboundHandler{

    private ClientSessionRegistry clientSessionRegistry;
    private ProtobufMessageRegistry messageRegistry;
    private MessageListener messageListener;

    public ProtobufTcpInboundHandler(ClientSessionRegistry clientSessionRegistry, ProtobufMessageRegistry messageRegistry, MessageListener messageListener) {
        this.clientSessionRegistry = clientSessionRegistry;
        this.messageRegistry = messageRegistry;
        this.messageListener = messageListener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        int typeId = msg.readInt();
        byte[] messageBytes = ByteBufUtils.remainingBytes(msg);
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
    public void send(ChannelHandlerContext context, byte[] data) {
        ByteBuf byteBuf = context.alloc().buffer();
        byteBuf.readBytes(data);
        context.writeAndFlush(data);
    }
}
