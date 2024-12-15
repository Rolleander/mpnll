package com.broll.mpnll.server.inbound;

import com.google.protobuf.Message;

import java.nio.ByteBuffer;

import io.netty.buffer.ByteBuf;

public final class ByteBufUtils {

    public static byte[] toMessageBytes(int type, Message message) {
        byte[] data = message.toByteArray();
        ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
        buffer.putInt(type);
        buffer.put(data);
        return buffer.array();
    }

    public static byte[] remainingBytes(ByteBuf byteBuf){
        int messageLength = byteBuf.readableBytes();
        byte[] messageBytes = new byte[messageLength];
        byteBuf.readBytes(messageBytes);
        return messageBytes;
    }
}
