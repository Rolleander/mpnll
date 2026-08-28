package com.broll.mpnll.message;

import com.google.protobuf.Any;
import com.google.protobuf.Message;

public interface MessageRegistry {

    Message parseMessage(byte[] bytes, int type);

    int getType(Message message);

    Any pack(Object value);

    Object unpack(Any value);

    <T> T unpack(Any value, Class<T> objectType);
}
