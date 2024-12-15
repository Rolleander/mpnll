package com.broll.mpnll.server;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;

public interface ProtobufMessageRegistry {

    Message parseMessage(byte[] bytes, int type);

    int getType(Message message);
}
