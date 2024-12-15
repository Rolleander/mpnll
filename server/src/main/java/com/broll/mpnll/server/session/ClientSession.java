package com.broll.mpnll.server.session;

import com.broll.mpnll.server.ProtobufMessageRegistry;
import com.broll.mpnll.server.inbound.ByteBufUtils;
import com.broll.mpnll.server.inbound.ClientInboundHandler;
import com.google.protobuf.Message;

import io.netty.channel.ChannelHandlerContext;

public class ClientSession {

    private ProtobufMessageRegistry messageRegistry;
    private ChannelHandlerContext context;
    private ClientInboundHandler handler;

    public ClientSession(
        ProtobufMessageRegistry messageRegistry,
        ChannelHandlerContext context,
        ClientInboundHandler handler) {
        this.messageRegistry = messageRegistry;
        this.context = context;
        this.handler = handler;
    }

    public void send(byte[] data){
        this.handler.send(this.context, data);
    }

    public void send(Message message){
        int type = messageRegistry.getType(message);
        this.send(ByteBufUtils.toMessageBytes(type, message));
    }
}