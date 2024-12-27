package com.broll.mpnll.server.connection;

import com.broll.mpnll.message.MessageRegistry;
import com.broll.mpnll.message.MessageUtils;
import com.broll.mpnll.server.inbound.ClientInboundHandler;
import com.broll.mpnll.server.user.User;
import com.google.protobuf.Message;

import java.net.InetSocketAddress;

import io.netty.channel.ChannelHandlerContext;

public class ClientConnection {

    private final MessageRegistry messageRegistry;
    private final ChannelHandlerContext context;
    private final ClientInboundHandler handler;
    private User user;
    private String ip;

    public ClientConnection(
        MessageRegistry messageRegistry,
        ChannelHandlerContext context,
        ClientInboundHandler handler) {
        this.messageRegistry = messageRegistry;
        this.context = context;
        this.handler = handler;
        this.initIp();
    }

    private void initIp() {
        InetSocketAddress remoteAddress = (InetSocketAddress) context.channel().remoteAddress();
        ip = remoteAddress.getAddress().getHostAddress();
    }

    void inactive() {
        if (user != null) {
            user.disconnect();
        }
    }

    public void connectWith(User user) {
        this.user = user;
        this.user.connect(this);
    }

    public User getUser() {
        return user;
    }

    public void send(byte[] data) {
        this.handler.send(this.context, data);
    }

    public void send(Message message) {
        this.send(MessageUtils.toMessageBytes(messageRegistry, message));
    }

    public void close() {
        this.context.close();
    }

    public String getIp() {
        return ip;
    }
}
