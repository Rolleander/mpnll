package com.broll.mpnll.message;

import com.google.protobuf.Message;

public interface MessageRegistrySetup {

    void register(Message.Builder builder);
}
