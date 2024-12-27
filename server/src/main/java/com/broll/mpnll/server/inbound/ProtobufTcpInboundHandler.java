package com.broll.mpnll.server.inbound;

import com.broll.mpnll.message.MessageRegistry;
import com.broll.mpnll.server.connection.ClientConnection;
import com.broll.mpnll.server.connection.ClientConnectionRegistry;
import com.google.protobuf.Message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ProtobufTcpInboundHandler extends SimpleChannelInboundHandler<ByteBuf> implements ClientInboundHandler {

    private ClientConnectionRegistry clientConnectionRegistry;
    private MessageRegistry messageRegistry;
    private MessageListener messageListener;

    public ProtobufTcpInboundHandler(ClientConnectionRegistry clientConnectionRegistry, MessageRegistry messageRegistry, MessageListener messageListener) {
        this.clientConnectionRegistry = clientConnectionRegistry;
        this.messageRegistry = messageRegistry;
        this.messageListener = messageListener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        int typeId = msg.readInt();
        byte[] messageBytes = ByteBufUtils.remainingBytes(msg);
        Message message = messageRegistry.parseMessage(messageBytes, typeId);
        ClientConnection session = clientConnectionRegistry.get(ctx);
        this.messageListener.received(session, message);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.clientConnectionRegistry.register(ctx, this);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.clientConnectionRegistry.remove(ctx);
    }

    @Override
    public void send(ChannelHandlerContext context, byte[] data) {
        ByteBuf byteBuf = context.alloc().buffer();
        byteBuf.readBytes(data);
        context.writeAndFlush(data);
    }
}
