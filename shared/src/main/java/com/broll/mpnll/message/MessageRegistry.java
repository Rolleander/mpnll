package com.broll.mpnll.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

public interface MessageRegistry {

    Message parseMessage(byte[] bytes, int type) throws InvalidProtocolBufferException;

    int getType(Message message);
}
