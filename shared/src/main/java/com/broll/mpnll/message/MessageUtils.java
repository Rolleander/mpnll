package com.broll.mpnll.message;

import com.google.protobuf.Message;

public final class MessageUtils {

    public static byte[] toMessageBytes(MessageRegistry messageRegistry, Message message) {
        int type = messageRegistry.getType(message);
        return MessageUtils.toMessageBytes(type, message);
    }

    public static byte[] toMessageBytes(int type, Message message) {
        byte[] data = message.toByteArray();
        byte[] result = new byte[4 + data.length];
        result[0] = (byte) (type >>> 24);
        result[1] = (byte) (type >>> 16);
        result[2] = (byte) (type >>> 8);
        result[3] = (byte) type;
        System.arraycopy(data, 0, result, 4, data.length);
        return result;
    }

    public static int getMessageType(byte[] data) {
        return ((data[0] & 255) << 24)
            | ((data[1] & 255) << 16)
            | ((data[2] & 255) << 8)
            | (data[3] & 255);
    }

    public static byte[] getMessageContent(byte[] data) {
        byte[] remainingBytes = new byte[data.length - 4];
        System.arraycopy(data, 4, remainingBytes, 0, remainingBytes.length);
        return remainingBytes;
    }

}
