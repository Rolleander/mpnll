package com.broll.mpnll.message;

import com.google.protobuf.Message;
import com.google.protobuf.Parser;

public interface MessageRegistrySetup {

     <T extends Message> void  register(Message.Builder builder, Parser<T> parser);
}
