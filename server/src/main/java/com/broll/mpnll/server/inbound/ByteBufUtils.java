package com.broll.mpnll.server.inbound;

import io.netty.buffer.ByteBuf;

public final class ByteBufUtils {

    public static byte[] remainingBytes(ByteBuf byteBuf) {
        int messageLength = byteBuf.readableBytes();
        byte[] messageBytes = new byte[messageLength];
        byteBuf.readBytes(messageBytes);
        return messageBytes;
    }
}
