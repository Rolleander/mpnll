package com.broll.mpnll.message;

import com.google.protobuf.Message;

import java.nio.ByteBuffer;

public final class MessageUtils {

    public static byte[] toMessageBytes(int type, Message message) {
        byte[] data = message.toByteArray();
        ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
        buffer.putInt(type);
        buffer.put(data);
        return buffer.array();
    }

    public static int getMessageType(byte[] data) {
        return ByteBuffer.wrap(data, 0, 4).getInt();
    }

    public static byte[] getMessageContent(byte[] data) {
        byte[] remainingBytes = new byte[data.length - 4];
        System.arraycopy(data, 4, remainingBytes, 0, remainingBytes.length);
        return remainingBytes;
    }

}
